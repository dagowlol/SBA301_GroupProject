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
            message.setSubject("Mã OTP của bạn — Auction System");
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
                Xin chào,
                Mã OTP của bạn là:
                    %s
                Mã này sẽ hết hạn sau 5 phút.
                Vui lòng không chia sẻ mã này với bất kỳ ai.
                Trân trọng,
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
            message.setSubject("Thông báo: Mật khẩu của bạn đã được thay đổi");
            message.setText("Xin chào,\n\nMật khẩu tài khoản của bạn trên hệ thống Auction System vừa được thay đổi thành công.\nNếu bạn không thực hiện yêu cầu này, vui lòng liên hệ ngay với bộ phận hỗ trợ.\n\nTrân trọng,\nAuction System Team");
            mailSender.send(message);
            log.info("Password changed email sent to: {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send password changed email to {}: {}", toEmail, e.getMessage());
        }
    }
}
