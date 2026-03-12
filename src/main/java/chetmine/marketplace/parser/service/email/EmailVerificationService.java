package chetmine.marketplace.parser.service.email;

import chetmine.marketplace.parser.service.UserService;
import chetmine.marketplace.parser.service.risk.token.EmailCodeRiskTokenService;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final UserService userService;
    private final EmailCodeRiskTokenService emailCodeRiskTokenService;
    private final EmailSenderService emailSenderService;

    Logger logger = LoggerFactory.getLogger(EmailVerificationService.class);

    public String sendCode(String email) throws MessagingException {

        if (!userService.existsEmail(email)) {
            throw new EntityNotFoundException("User must exist");
        }

        String code = String.format("%06d", new Random().nextInt(999999));
        String token = emailCodeRiskTokenService.issue(email, code);

        emailSenderService.sendVerificationCode(email, code);

        logger.info("Email sent with code: {}", code);

        return token;
    }

    public String verifyCode(String riskToken, String code) {
        String email = emailCodeRiskTokenService.verify(riskToken, code);

        return email;
    }
}
