package hoang.com.auction_system_be.service.common.otp;

import hoang.com.auction_system_be.entity.OtpRecord;
import hoang.com.auction_system_be.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {
    private final OtpRepository otpRepository;
    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;
    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public String generateOtp(String email) {
        otpRepository.deleteByEmail(email);
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        OtpRecord record = OtpRecord.builder()
                .email(email)
                .otpCode(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .used(false)
                .build();
        otpRepository.save(record);
        log.debug("OTP generated for email: {}", email);
        return otp;
    }

    @Override
    @Transactional
    public boolean verifyOtp(String email, String otp) {
        return otpRepository.findByEmailAndOtpCodeAndUsedFalse(email, otp)
                .filter(r -> r.getExpiryTime().isAfter(LocalDateTime.now()))
                .map(r -> {
                    r.setUsed(true);
                    otpRepository.save(r);
                    log.debug("OTP verified for email: {}", email);
                    return true;
                })
                .orElse(false);
    }
}
