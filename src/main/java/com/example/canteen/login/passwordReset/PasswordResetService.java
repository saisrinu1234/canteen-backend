package com.example.canteen.login.passwordReset;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.canteen.login.User1;
import com.example.canteen.login.UserRepository;
import com.example.canteen.mailing.EmailService;

import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

        private final UserRepository userRepository;
        private final PasswordResetTokenRepository tokenRepository;
        private final PasswordEncoder passwordEncoder;
        private final EmailService emailService;

        public PasswordResetService(
                        UserRepository userRepository,
                        PasswordResetTokenRepository tokenRepository,
                        PasswordEncoder passwordEncoder,
                        EmailService emailService) {

                this.userRepository = userRepository;
                this.tokenRepository = tokenRepository;
                this.passwordEncoder = passwordEncoder;
                this.emailService = emailService;
        }

        @Transactional
        public void forgotPassword(String email) {

                User1 user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Remove old reset tokens
                tokenRepository.deleteAllByUserId(user.getId());
                tokenRepository.flush();

                // Generate secure random token
                String token = UUID.randomUUID().toString();

                // Token expires after 15 minutes
                LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);

                PasswordResetToken resetToken = new PasswordResetToken(
                                token,
                                user,
                                expiryDate);

                tokenRepository.save(resetToken);

                String resetLink = "https://canteen-ui-lilac.vercel.app/reset-password?token="
                                + token;
                try {
                        emailService.sendPasswordResetEmail(
                                        user.getEmail(),
                                        resetLink);
                } catch (Exception e) {
                        System.err.println(
                                        "Password reset email failed: "
                                                        + e.getMessage());
                        throw new RuntimeException(
                                        "Unable to send password reset email");

                }
        }

        public void resetPassword(
                        String token,
                        String newPassword) {

                PasswordResetToken resetToken = tokenRepository.findByToken(token)
                                .orElseThrow(() -> new RuntimeException(
                                                "Invalid reset token"));

                if (resetToken.isUsed()) {
                        throw new RuntimeException(
                                        "Reset link has already been used");
                }

                if (resetToken.getExpiryDate()
                                .isBefore(LocalDateTime.now())) {

                        throw new RuntimeException(
                                        "Reset link has expired");
                }

                User1 user = resetToken.getUser();

                user.setPassword(
                                passwordEncoder.encode(newPassword));

                userRepository.save(user);

                // Make token unusable
                resetToken.setUsed(true);

                tokenRepository.save(resetToken);
        }
}
