package chetmine.marketplace.parser.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSenderService {
    private final JavaMailSender sender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationCode(String toEmail, String code) throws MessagingException {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Код подтверждения");
        helper.setText(this.buildEmailBody(code), true);

        sender.send(message);
    }

    private String buildEmailBody(String code) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 400px;">
                <h2>Подтверждение почты</h2>
                <p>Ваш код подтверждения:</p>
                <div style="font-size: 32px; font-weight: bold; letter-spacing: 8px;">
                    %s
                </div>
                <p style="color: #888;">Код действителен 10 минут.</p>
            </div>
            """.formatted(code);
    }
}
