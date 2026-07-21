package hoang.com.auction_system_be.service.common.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        // [DEVELOPMENT USE] Print to console for easy Postman testing without SMTP
        // config
        log.info("==========================================================");
        log.info("DEVELOPMENT OTP: Code for {} is: {}", toEmail, otp);
        log.info("==========================================================");

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Auction System verification code");
            message.setText(buildOtpEmailText(otp));
            mailSender.send(message);
            log.info("OTP email sent to: {}", toEmail);
        } catch (Exception e) {
            log.warn(
                    "Failed to send OTP email to {}: {}. (Please check SMTP config in application.properties if you want real emails)",
                    toEmail, e.getMessage());
        }
    }

    private String buildOtpEmailText(String otp) {
        return """
                Hello,
                Your verification code is:
                    %s
                This code expires in 5 minutes.
                Do not share this code with anyone.
                Regards,
                Auction System Team
                """.formatted(otp);
    }

    @Async
    @Override
    public void sendPasswordChangedEmail(String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Auction System password was changed");
            message.setText("Hello,\n\nYour Auction System password was changed successfully.\nIf you did not make this change, contact support immediately.\n\nRegards,\nAuction System Team");
            mailSender.send(message);
            log.info("Password changed email sent to: {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send password changed email to {}: {}", toEmail, e.getMessage());
        }
    }
}
