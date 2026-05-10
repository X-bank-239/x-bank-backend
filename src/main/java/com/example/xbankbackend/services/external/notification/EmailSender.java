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
import java.math.BigDecimal;
import java.time.LocalDate;
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

        sendVerificationCodeEmail(email, code);

        TempAuthState state = new TempAuthState();
        state.setId(stateId);
        state.setUserId(userId);
        state.setEmail(email);
        state.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
        return state;
    }

    public void sendLoanRepaymentReceipt(String to, UUID loanId, BigDecimal amount, BigDecimal outstandingPrincipal,
                                         LocalDate nextPaymentDate, boolean loanClosed) {
        String subject = "Платеж по кредиту X-Bank";
        String nextPaymentText = nextPaymentDate == null ? "не требуется (кредит закрыт)" : nextPaymentDate.toString();
        String statusText = loanClosed ? "Кредит закрыт" : "Кредит активен";
        String html = "Платеж по кредиту принят.<br/>"
                + "ID кредита: <b>" + loanId + "</b><br/>"
                + "Сумма платежа: <b>" + amount + "</b><br/>"
                + "Остаток основного долга: <b>" + outstandingPrincipal + "</b><br/>"
                + "Следующая дата платежа: <b>" + nextPaymentText + "</b><br/>"
                + "Статус: <b>" + statusText + "</b>";
        sendHtmlEmail(to, subject, html);
    }

    private void sendVerificationCodeEmail(String to, String code) {
        sendHtmlEmail(to, "Код подтверждения X-Bank",
                "Ваш код для входа: <b>" + code + "</b>. Действует 5 минут.");
    }

    private void sendHtmlEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Не удалось отправить email", e);
        }
    }
}
