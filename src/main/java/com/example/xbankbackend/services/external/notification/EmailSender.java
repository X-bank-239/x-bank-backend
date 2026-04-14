package com.example.xbankbackend.services.external.notification;

import com.example.xbankbackend.dtos.TempAuthState;
import com.example.xbankbackend.repositories.VerificationCodesRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.UUID;

@AllArgsConstructor
@Service
public class EmailSender {

    private JavaMailSender mailSender;
    private VerificationCodesRepository verificationCodesRepository;

    public TempAuthState sendVerificationCode(UUID userId, String email) {
        String code = String.format("%06d", new SecureRandom().nextInt(999999));
        String hashedCode = BCrypt.hashpw(code, BCrypt.gensalt());
        UUID stateId = verificationCodesRepository.create(userId, hashedCode);

        sendEmail(email, code);

        TempAuthState state = new TempAuthState();
        state.setId(stateId);
        state.setUserId(userId);
        state.setEmail(email);
        state.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
        return state;
    }

    private void sendEmail(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Код подтверждения X-Bank");
            helper.setText("Ваш код для входа: <b>" + code + "</b>. Действует 5 минут.", true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
