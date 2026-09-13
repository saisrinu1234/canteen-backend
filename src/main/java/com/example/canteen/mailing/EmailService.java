package com.example.canteen.mailing;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    private final SecureRandom random = new SecureRandom();

    private final Map<String, OtpDetails> otpStorage = new ConcurrentHashMap<>();

    // Constructor injection
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String email) throws Exception {

        String otp = generateOtp();

        otpStorage.put(
                email,
                new OtpDetails(
                        otp,
                        LocalDateTime.now().plusMinutes(5)));

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(senderEmail);

        helper.setTo(email);

        helper.setSubject("Canteen Email Verification");

        String html = """
                <div style="font-family:Arial;padding:20px">

                <h2 style="color:#ff9800;">
                    🍔 Canteen Verification
                </h2>

                <p>Hello,</p>

                <p>Your verification code is</p>

                <h1 style="
                    color:#2196F3;
                    letter-spacing:5px;">
                %s
                </h1>

                <p>
                    This OTP will expire in
                    <b>5 minutes</b>.
                </p>

                <hr>

                <p style="color:gray;">
                If you didn't request this,
                ignore this email.
                </p>

                </div>
                """.formatted(otp);

        helper.setText(html, true);

        mailSender.send(message);
    }

    public boolean verifyOtp(String email, String otp) {

        OtpDetails details = otpStorage.get(email);

        if (details == null)
            return false;

        if (details.getExpiryTime()
                .isBefore(LocalDateTime.now())) {

            otpStorage.remove(email);

            return false;
        }

        if (!details.getOtp().equals(otp))
            return false;

        otpStorage.remove(email);

        return true;
    }

    private String generateOtp() {

        return String.format("%06d",
                random.nextInt(1000000));
    }

    public void sendPasswordResetEmail(
            String email,
            String resetLink) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(senderEmail);
        helper.setTo(email);
        helper.setSubject("Reset Your Password");

        helper.setText(
                "Hello,\n\n"
                        + "We received a request to reset your password.\n\n"
                        + "Click the link below to reset your password:\n\n"
                        + resetLink + "\n\n"
                        + "This link will expire in 15 minutes.\n\n"
                        + "If you did not request a password reset, "
                        + "please ignore this email.\n\n"
                        + "Regards,\n"
                        + "Canteen Verification");

        mailSender.send(message);
    }

}
