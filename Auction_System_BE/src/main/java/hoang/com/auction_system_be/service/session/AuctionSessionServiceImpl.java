package hoang.com.auction_system_be.service.session;

import hoang.com.auction_system_be.dto.request.AuctionSessionRequest;
import hoang.com.auction_system_be.dto.request.AuctionSessionUpdateRequest;
import hoang.com.auction_system_be.dto.request.PlaceBidRequest;
import hoang.com.auction_system_be.dto.response.AuctionSessionDetailResponse;
import hoang.com.auction_system_be.dto.response.AuctionSessionResponse;
import hoang.com.auction_system_be.dto.response.BidBroadcastResponse;
import hoang.com.auction_system_be.dto.response.BidLogResponse;
import hoang.com.auction_system_be.entity.*;
import hoang.com.auction_system_be.enums.SessionStatus;
import hoang.com.auction_system_be.enums.BidStatus;
import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;
import hoang.com.auction_system_be.mapper.AuctionSessionMapper;
import hoang.com.auction_system_be.mapper.BidMapper;
import hoang.com.auction_system_be.enums.ItemStatus;
import hoang.com.auction_system_be.event.AuditLogEvent;
import hoang.com.auction_system_be.event.AuctionSessionCreatedEvent;
import hoang.com.auction_system_be.dto.response.CursorPageResponse;
import hoang.com.auction_system_be.dto.response.AuctionSessionListResponse;
import hoang.com.auction_system_be.event.BidPlacedEvent;
import hoang.com.auction_system_be.event.SessionExtendedEvent;
import hoang.com.auction_system_be.repository.*;
import hoang.com.auction_system_be.service.auth.SecurityContextService;
import hoang.com.auction_system_be.service.storage.ObjectStorageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import hoang.com.auction_system_be.service.DistributedLockService;
import hoang.com.auction_system_be.service.autobid.AutoBidService;
import hoang.com.auction_system_be.service.session.detector.SuspiciousBidDetector;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuctionSessionServiceImpl implements AuctionSessionService {

        AuctionSessionRepository auctionSessionRepository;
        BidRepository bidRepository;
        UserRepository userRepository;
        AuctionItemRepository auctionItemRepository;
        AuctionParticipantRepository auctionParticipantRepository;
        AuctionExtensionLogRepository auctionExtensionLogRepository;
        AuctionSessionMapper auctionSessionMapper;
        BidMapper bidMapper;
        SecurityContextService authenticationService;
        ApplicationEventPublisher eventPublisher;
        TransactionTemplate transactionTemplate;
        DistributedLockService lockService;
        AutoBidService autoBidService;
        ObjectStorageService objectStorageService;
        SuspiciousBidDetector suspiciousBidDetector;

        // ─── WebSocket / Bid Logic ────────────────────────────────────────────

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "auction_session_detail", key = "'detail_' + #sessionId")
        public AuctionSessionDetailResponse getAuctionSessionDetail(Long sessionId) {
                AuctionSession session = auctionSessionRepository.findByIdWithDetails(sessionId)
                                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));

                AuctionItem item = session.getItem();

                String imageUrl = resolvePrimaryImageUrl(item);
                String currentWinnerName = resolveWinnerName(session.getCurrentWinnerParticipant());

                List<Bid> recentBids = bidRepository
                                .findTop10ByParticipantSessionIdAndStatusNotOrderByBidTimestampDesc(
                                                sessionId, BidStatus.CANCELLED);

                List<BidLogResponse> bidLogs = recentBids.stream()
                                .map(bidMapper::toBidLogResponse)
                                .collect(Collectors.toList());

                log.info("session status {}", session.getStatus());
                return auctionSessionMapper.toDetailResponse(session, imageUrl, currentWinnerName, bidLogs);
        }

        @Override
        @Transactional
        @Caching(evict = {
                        @CacheEvict(value = "auction_session_detail", key = "'basic_' + #sessionId"),
                        @CacheEvict(value = "auction_session_detail", key = "'detail_' + #sessionId")
        })
        public void placeBid(Long sessionId, PlaceBidRequest request) {
                try {
                        AuctionSession session = getSessionWithLock(sessionId);
                        validateSessionStateAndBidAmount(session, request.getBidAmount());

                        User user = userRepository.findById(request.getUserId())
                                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                        AuctionParticipant participant = resolveParticipant(user, session);

                        LocalDateTime now = LocalDateTime.now();
                        boolean isSuspicious = suspiciousBidDetector.detect(session, user, request.getBidAmount(), now);
                        saveBidRecord(participant, request.getBidAmount(), now, isSuspicious);

                        updateSessionAndHandleAntiSnipe(session, participant, request.getBidAmount(), now);

                        log.info("Bid placed successfully - session: {}, user: {}, amount: {}",
                                        sessionId, request.getUserId(), request.getBidAmount());

                        broadcastBidEvent(session, user, request.getBidAmount(), now);
                        autoBidService.triggerAutoBids(sessionId);

                } catch (AppException e) {
                        log.warn("Bid validation failed for session {}: {}", sessionId, e.getMessage());
                        eventPublisher.publishEvent(new BidPlacedEvent(this, request.getUserId().toString(),
                                        e.getErrorCode().getMessage()));
                        throw e;
                }
        }

        private AuctionSession getSessionWithLock(Long sessionId) {
                return auctionSessionRepository.findByIdWithPessimisticLock(sessionId)
                                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));
        }

        private void validateSessionStateAndBidAmount(AuctionSession session, BigDecimal bidAmount) {
                if (session.getStatus() != SessionStatus.ACTIVE) {
                        throw new AppException(ErrorCode.SESSION_NOT_ACTIVE);
                }
                if (LocalDateTime.now().isAfter(session.getEndTime())) {
                        boolean reserveMet = isReserveMet(session);
                        session.setStatus(reserveMet ? SessionStatus.ENDED : SessionStatus.RESERVE_NOT_MET);
                        auctionSessionRepository.save(session);
                        throw new AppException(ErrorCode.AUCTION_ENDED);
                }

                BigDecimal currentPrice = session.getCurrentHighestBid() != null
                                ? session.getCurrentHighestBid()
                                : session.getItem().getStartingPrice();

                BigDecimal minimumBid = currentPrice.add(session.getMinimumIncrement());

                if (bidAmount.compareTo(minimumBid) < 0) {
                        throw new AppException(ErrorCode.INVALID_BID_AMOUNT);
                }
        }

        private boolean isReserveMet(AuctionSession session) {
                BigDecimal highestBid = session.getCurrentHighestBid();
                BigDecimal reservePrice = session.getReservePrice();
                if (highestBid == null) return false;
                if (reservePrice == null) return true;
                return highestBid.compareTo(reservePrice) >= 0;
        }

        private AuctionParticipant resolveParticipant(User user, AuctionSession session) {
                return auctionParticipantRepository
                                .findByUserIdAndSessionId(user.getId(), session.getId())
                                .orElseGet(() -> auctionParticipantRepository.save(
                                                auctionSessionMapper.toParticipant(user, session)));
        }

        private void saveBidRecord(AuctionParticipant participant, BigDecimal bidAmount, LocalDateTime now, boolean isSuspicious) {
                Bid bid = bidMapper.toBid(participant, bidAmount, now);
                bid.setSuspicious(isSuspicious);
                bidRepository.save(bid);
        }

        private void updateSessionAndHandleAntiSnipe(AuctionSession session, AuctionParticipant participant,
                        BigDecimal bidAmount, LocalDateTime now) {
                session.setCurrentHighestBid(bidAmount);
                session.setCurrentWinnerParticipant(participant);
                session.setBidCount(session.getBidCount() + 1);

                LocalDateTime oldEndTime = session.getEndTime();
                long secondsRemaining = Duration.between(now, oldEndTime).getSeconds();

                if (secondsRemaining <= session.getAntiSnipeWindowSeconds()) {
                        LocalDateTime newEndTime = oldEndTime.plusSeconds(session.getAntiSnipeExtensionSeconds());
                        session.setEndTime(newEndTime);

                        AuctionExtensionLog extensionLog = auctionSessionMapper.toExtensionLog(
                                        session, participant, oldEndTime, newEndTime,
                                        "Anti-snipe: bid placed within window");
                        auctionExtensionLogRepository.save(extensionLog);

                        log.info("Anti-snipe triggered for session {}: extended from {} to {}",
                                        session.getId(), oldEndTime, newEndTime);
                }

                auctionSessionRepository.save(session);
        }

        private void broadcastBidEvent(AuctionSession session, User user, BigDecimal bidAmount, LocalDateTime now) {
                String winnerName = user.getFirstName() + " " + user.getLastName();
                BidBroadcastResponse broadcastResponse = bidMapper.toBidBroadcastResponse(
                                session.getId(), bidAmount, winnerName, session.getEndTime(), now);

                eventPublisher.publishEvent(
                                new BidPlacedEvent(this, String.valueOf(session.getId()), broadcastResponse));
        }

        // ─── Staff/Admin CRUD ─────────────────────────────────────────────────

        @Override
        public AuctionSessionResponse createSession(AuctionSessionRequest request) {
                return lockService.executeWithLock("item:" + request.getItemId(), () -> {
                        return transactionTemplate.execute(status -> doCreateSession(request));
                });
        }

        private AuctionSessionResponse doCreateSession(AuctionSessionRequest request) {
                Long itemId = request.getItemId();

                AuctionItem item = auctionItemRepository.findById(itemId)
                                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

                if (item.getStatus() == ItemStatus.SOLD) {
                        throw new AppException(ErrorCode.ITEM_ALREADY_SOLD);
                }
                if (item.getStatus() == ItemStatus.PAID) {
                        throw new AppException(ErrorCode.ITEM_ALREADY_SOLD);
                }
                if (item.getStatus() != ItemStatus.APPROVED) {
                        throw new AppException(ErrorCode.ITEM_NOT_APPROVED);
                }

                List<SessionStatus> liveStatuses = List.of(SessionStatus.SCHEDULED, SessionStatus.ACTIVE);
                if (auctionSessionRepository.existsByItemIdAndStatusIn(itemId, liveStatuses)) {
                        throw new AppException(ErrorCode.SESSION_CONFLICT);
                }

                if (auctionSessionRepository.existsByItemIdAndStatusIn(itemId, List.of(SessionStatus.PAID))) {
                        throw new AppException(ErrorCode.ITEM_ALREADY_SOLD);
                }

                Long staffId = authenticationService.getCurrentUserId();
                User staff = userRepository.findById(staffId)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                // ── 6. Persist session ────────────────────────────────────────────────
                AuctionSession session = AuctionSession.builder()
                                .item(item)
                                .startTime(request.getStartTime())
                                .endTime(request.getEndTime())
                                .reservePrice(request.getReservePrice())
                                .minimumIncrement(request.getMinimumIncrement())
                                .antiSnipeWindowSeconds(request.getAntiSnipeWindowSeconds() != null
                                                ? request.getAntiSnipeWindowSeconds()
                                                : 10)
                                .antiSnipeExtensionSeconds(request.getAntiSnipeExtensionSeconds() != null
                                                ? request.getAntiSnipeExtensionSeconds()
                                                : 30)
                                .status(SessionStatus.SCHEDULED)
                                .createdBy(staff)
                                .build();

                AuctionSession saved;
                try {
                        saved = auctionSessionRepository.save(session);
                } catch (DataIntegrityViolationException ex) {
                        throw new AppException(ErrorCode.SESSION_CONFLICT);
                }

                log.info("Created auction session id={} for item={} by staff={}", saved.getId(), itemId, staffId);

                String auditDetails = String.format("item=%d, start=%s, end=%s, reservePrice=%s",
                                itemId, request.getStartTime(), request.getEndTime(), request.getReservePrice());
                eventPublisher.publishEvent(new AuditLogEvent(this, staffId, AuditLogEvent.ACTION_SESSION_CREATED,
                                AuditLogEvent.ENTITY_AUCTION_SESSION, saved.getId(), auditDetails));

                // ── 6. Publish Event AFTER_COMMIT (decoupled side-effects) ─────────────
                eventPublisher.publishEvent(new AuctionSessionCreatedEvent(this, saved, staffId));

                return toResponse(saved);
        }

        @Override
        @Transactional(readOnly = true)
        public CursorPageResponse<AuctionSessionListResponse> getSessions(Long cursor, int size, String search,
                        SessionStatus status) {
                String cleanSearch = (search != null) ? search.trim() : "";

                if (!cleanSearch.isEmpty() && cleanSearch.length() < 2) {
                        throw new AppException(ErrorCode.SEARCH_KEYWORD_TOO_SHORT);
                }

                Pageable pageable = PageRequest.of(0, size + 1);

                boolean hasCursor = cursor != null;
                Long safeCursor = hasCursor ? cursor : 0L;

                boolean hasStatus = status != null;
                SessionStatus safeStatus = hasStatus ? status : SessionStatus.SCHEDULED;

                boolean hasSearch = !cleanSearch.isEmpty();

                List<AuctionSessionListResponse> sessions = auctionSessionRepository.findOptimizedSessions(
                                hasCursor, safeCursor, hasStatus, safeStatus, hasSearch, cleanSearch, pageable);

                boolean hasNext = sessions.size() > size;
                if (hasNext) {
                        sessions.remove(sessions.size() - 1);
                }

                Long nextCursor = sessions.isEmpty() ? null : sessions.get(sessions.size() - 1).getId();

                return CursorPageResponse.<AuctionSessionListResponse>builder()
                                .content(sessions)
                                .nextCursor(nextCursor)
                                .hasNext(hasNext)
                                .pageSize(size)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "auction_session_detail", key = "'basic_' + #id")
        public AuctionSessionResponse getSessionById(Long id) {
                AuctionSession session = auctionSessionRepository.findByIdWithDetails(id)
                                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));
                return toResponse(session);
        }

        @Override
        @Transactional
        @Caching(evict = {
                        @CacheEvict(value = "auction_session_detail", key = "'basic_' + #id"),
                        @CacheEvict(value = "auction_session_detail", key = "'detail_' + #id")
        })
        public AuctionSessionResponse updateSession(Long id, AuctionSessionUpdateRequest request) {
                AuctionSession session = auctionSessionRepository.findByIdWithDetails(id)
                                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));

                boolean isActive = session.getStatus() == SessionStatus.ACTIVE;

                // 1. Effective State & Time Calculation
                LocalDateTime effectiveStartTime = request.getStartTime() != null ? request.getStartTime()
                                : session.getStartTime();
                LocalDateTime effectiveEndTime = request.getEndTime() != null ? request.getEndTime()
                                : session.getEndTime();
                SessionStatus effectiveStatus = request.getStatus() != null ? request.getStatus() : session.getStatus();

                // 2. Start time can only be changed for SCHEDULED sessions
                if (request.getStartTime() != null && session.getStatus() != SessionStatus.SCHEDULED) {
                        throw new AppException(ErrorCode.SESSION_CANNOT_UPDATE_START_TIME);
                }

                // 3. Strict Time Validation for SCHEDULED and ACTIVE
                if (effectiveStatus == SessionStatus.SCHEDULED || effectiveStatus == SessionStatus.ACTIVE) {
                        if (!effectiveEndTime.isAfter(LocalDateTime.now())) {
                                throw new AppException(ErrorCode.SESSION_CANNOT_ACTIVATE_PAST_END_TIME);
                        }
                        if (!effectiveEndTime.isAfter(effectiveStartTime)) {
                                throw new AppException(ErrorCode.SESSION_INVALID_END_TIME);
                        }
                }

                // 4. Apply Updates
                if (request.getStartTime() != null) {
                        session.setStartTime(request.getStartTime());
                }
                if (request.getEndTime() != null) {
                        session.setEndTime(request.getEndTime());
                }

                if (request.getStatus() != null && request.getStatus() != session.getStatus()) {
                        if (request.getStatus() == SessionStatus.CANCELLED && request.getCancellationReason() != null) {
                                session.setCancellationReason(request.getCancellationReason());
                        }
                        session.setStatus(request.getStatus());
                }

                // 4. Financial & Strategy Parameters
                if (request.getReservePrice() != null) {
                        session.setReservePrice(request.getReservePrice());
                }
                if (request.getMinimumIncrement() != null) {
                        session.setMinimumIncrement(request.getMinimumIncrement());
                }
                if (request.getAntiSnipeWindowSeconds() != null) {
                        session.setAntiSnipeWindowSeconds(request.getAntiSnipeWindowSeconds());
                }
                if (request.getAntiSnipeExtensionSeconds() != null) {
                        session.setAntiSnipeExtensionSeconds(request.getAntiSnipeExtensionSeconds());
                }

                AuctionSession updated = auctionSessionRepository.save(session);
                log.info("Updated auction session id={}, new status={}", id, updated.getStatus());

                Long staffId = authenticationService.getCurrentUserId();
                if (staffId != null) {
                        String details = String.format("status=%s, endTime=%s", updated.getStatus(),
                                        updated.getEndTime());
                        eventPublisher.publishEvent(
                                        new AuditLogEvent(this, staffId, AuditLogEvent.ACTION_SESSION_UPDATED,
                                                        AuditLogEvent.ENTITY_AUCTION_SESSION, id, details));
                }

                // Event Broadcasting
                if (isActive && request.getEndTime() != null) {
                        eventPublisher.publishEvent(new SessionExtendedEvent(this, String.valueOf(id), java.util.Map.of(
                                        "type", "SESSION_EXTENDED",
                                        "sessionId", id,
                                        "newEndTime", updated.getEndTime().toString())));
                }

                return toResponse(updated);
        }

        @Override
        @Transactional
        @Caching(evict = {
                        @CacheEvict(value = "auction_session_detail", key = "'basic_' + #id"),
                        @CacheEvict(value = "auction_session_detail", key = "'detail_' + #id")
        })
        public void deleteSession(Long id) {
                AuctionSession session = auctionSessionRepository.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));

                if (session.getStatus() == SessionStatus.ACTIVE) {
                        throw new AppException(ErrorCode.SESSION_ALREADY_ACTIVE);
                }

                auctionSessionRepository.delete(session); // soft delete via @SQLDelete
                log.info("Soft-deleted auction session id={}", id);

                Long staffId = authenticationService.getCurrentUserId();
                if (staffId != null) {
                        eventPublisher.publishEvent(new AuditLogEvent(this, staffId,
                                        AuditLogEvent.ACTION_SESSION_DELETED,
                                        AuditLogEvent.ENTITY_AUCTION_SESSION, id,
                                        "Soft-deleted session id=" + id));
                }
        }

        // ─── Private helper ───────────────────────────────────────────────────

        private AuctionSessionResponse toResponse(AuctionSession s) {
                AuctionItem item = s.getItem();

                String imageUrl = resolvePrimaryImageUrl(item);
                String winnerName = resolveWinnerName(s.getCurrentWinnerParticipant());

                String createdByName = null;
                Long createdById = null;
                if (s.getCreatedBy() != null) {
                        createdById = s.getCreatedBy().getId();
                        createdByName = s.getCreatedBy().getFirstName() + " " + s.getCreatedBy().getLastName();
                }

                return AuctionSessionResponse.builder()
                                .id(s.getId())
                                .itemId(item.getId())
                                .itemName(item.getName())
                                .itemDescription(item.getDescription())
                                .itemImage(imageUrl)
                                .createdById(createdById)
                                .createdByName(createdByName)
                                .startTime(s.getStartTime())
                                .endTime(s.getEndTime())
                                .reservePrice(s.getReservePrice())
                                .minimumIncrement(s.getMinimumIncrement())
                                .currentHighestBid(s.getCurrentHighestBid())
                                .currentWinnerName(winnerName)
                                .status(s.getStatus())
                                .antiSnipeWindowSeconds(s.getAntiSnipeWindowSeconds())
                                .antiSnipeExtensionSeconds(s.getAntiSnipeExtensionSeconds())
                                .bidCount(s.getBidCount())
                                .cancellationReason(s.getCancellationReason())
                                .createdAt(s.getCreatedAt())
                                .updatedAt(s.getUpdatedAt())
                                .build();
        }

        private String resolvePrimaryImageUrl(AuctionItem item) {
                if (item.getImages() == null)
                        return null;
                return item.getImages().stream()
                                .filter(ItemImage::isPrimary)
                                .findFirst()
                                .or(() -> item.getImages().stream().findFirst())
                                .map(ItemImage::getImageUrl)
                                .map(objectStorageService::resolveUrl)
                                .orElse(null);
        }

        private String resolveWinnerName(AuctionParticipant participant) {
                if (participant == null || participant.getUser() == null)
                        return null;
                User winner = participant.getUser();
                return winner.getFirstName() + " " + winner.getLastName();
        }

        @Override
        @Transactional(readOnly = true)
        public List<AuctionSessionListResponse> getDeletedSessions() {
                List<Object[]> rows = auctionSessionRepository.findDeletedSessionsRaw();
                List<AuctionSessionListResponse> result = new ArrayList<>();
                for (Object[] row : rows) {
                        result.add(AuctionSessionListResponse.builder()
                                        .id(((Number) row[0]).longValue())
                                        .itemId(((Number) row[1]).longValue())
                                        .itemName((String) row[2])
                                        .itemDescription((String) row[3])
                                        .reservePrice(row[4] != null ? new BigDecimal(row[4].toString()) : null)
                                        .currentHighestBid(row[5] != null ? new BigDecimal(row[5].toString()) : null)
                                        .status(SessionStatus.valueOf((String) row[6]))
                                        .startTime(row[7] instanceof Timestamp ? ((Timestamp) row[7]).toLocalDateTime()
                                                        : (LocalDateTime) row[7])
                                        .endTime(row[8] instanceof Timestamp ? ((Timestamp) row[8]).toLocalDateTime()
                                                        : (LocalDateTime) row[8])
                                        .build());
                }
                return result;
        }

        @Override
        @Transactional
        @Caching(evict = {
                        @CacheEvict(value = "auction_session_detail", key = "'basic_' + #id"),
                        @CacheEvict(value = "auction_session_detail", key = "'detail_' + #id")
        })
        public void restoreSession(Long id) {
                int updated = auctionSessionRepository.restoreSession(id);
                if (updated == 0) {
                        throw new AppException(ErrorCode.SESSION_NOT_FOUND);
                }
                log.info("Restored auction session id={}", id);
                Long staffId = authenticationService.getCurrentUserId();
                if (staffId != null) {
                        eventPublisher.publishEvent(new AuditLogEvent(this, staffId,
                                        AuditLogEvent.ACTION_SESSION_UPDATED,
                                        AuditLogEvent.ENTITY_AUCTION_SESSION, id,
                                        "Restored session id=" + id));
                }
        }
}
