package hoang.com.auction_system_be.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import hoang.com.auction_system_be.entity.IdempotencyRecord;
import hoang.com.auction_system_be.repository.IdempotencyRepository;
import hoang.com.auction_system_be.service.auth.SecurityContextService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class IdempotencyAspect {

    IdempotencyRepository idempotencyRepository;
    SecurityContextService authenticationService;
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Around("@annotation(hoang.com.auction_system_be.annotation.Idempotent)")
    public Object handleIdempotency(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return joinPoint.proceed();
        }

        String idempotencyKey = request.getHeader("Idempotency-Key");
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            log.warn("API annotated with @Idempotent but no Idempotency-Key header found.");
            return joinPoint.proceed();
        }

        Optional<IdempotencyRecord> existing = idempotencyRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            IdempotencyRecord rec = existing.get();
            log.debug("Idempotency hit! Key={}, returning cached response.", idempotencyKey);
            
            Object body = null;
            if (rec.getResponseBody() != null) {
                body = objectMapper.readValue(rec.getResponseBody(), Object.class);
            }
            return ResponseEntity.status(rec.getStatusCode()).body(body);
        }

        Object result = joinPoint.proceed();

        if (result instanceof ResponseEntity<?> responseEntity) {
            try {
                String responseBodyJson = null;
                if (responseEntity.getBody() != null) {
                    responseBodyJson = objectMapper.writeValueAsString(responseEntity.getBody());
                }

                Long userId = authenticationService.getCurrentUserId();
                if (userId == null) userId = 0L;

                idempotencyRepository.save(IdempotencyRecord.builder()
                        .idempotencyKey(idempotencyKey)
                        .statusCode(responseEntity.getStatusCode().value())
                        .responseBody(responseBodyJson)
                        .userId(userId)
                        .expiresAt(LocalDateTime.now().plusHours(24))
                        .build());
                        
                log.debug("Saved idempotency key={} for future deduplication.", idempotencyKey);
            } catch (DataIntegrityViolationException ex) {
                log.warn("Idempotency key {} already stored by a concurrent request.", idempotencyKey);
            } catch (Exception ex) {
                log.error("Failed to serialize and save idempotency record: {}", ex.getMessage());
            }
        }

        return result;
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return (attrs != null) ? attrs.getRequest() : null;
    }
}
