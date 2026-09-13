// // package com.example.canteen.mailing;

// // import java.security.SecureRandom;
// // import java.time.LocalDateTime;
// // import java.util.Map;
// // import java.util.concurrent.ConcurrentHashMap;
// // import org.springframework.beans.factory.annotation.Value;
// // import org.springframework.mail.SimpleMailMessage;
// // import org.springframework.mail.javamail.JavaMailSender;
// // import org.springframework.mail.javamail.MimeMessageHelper;
// // import org.springframework.stereotype.Service;

// // import jakarta.mail.internet.MimeMessage;

// // @Service
// // public class EmailService {

// //     private final JavaMailSender mailSender;

// //     @Value("${spring.mail.username}")
// //     private String senderEmail;

// //     private final SecureRandom random = new SecureRandom();

// //     private final Map<String, OtpDetails> otpStorage = new ConcurrentHashMap<>();

// //     // Constructor injection
// //     public EmailService(JavaMailSender mailSender) {
// //         this.mailSender = mailSender;
// //     }

// //     public void sendOtp(String email) throws Exception {

// //         String otp = generateOtp();

// //         otpStorage.put(
// //                 email,
// //                 new OtpDetails(
// //                         otp,
// //                         LocalDateTime.now().plusMinutes(5)));

// //         MimeMessage message = mailSender.createMimeMessage();

// //         MimeMessageHelper helper = new MimeMessageHelper(message, true);

// //         helper.setFrom(senderEmail);

// //         helper.setTo(email);

// //         helper.setSubject("Canteen Email Verification");

// //         String html = """
// //                 <div style="font-family:Arial;padding:20px">

// //                 <h2 style="color:#ff9800;">
// //                     🍔 Canteen Verification
// //                 </h2>

// //                 <p>Hello,</p>

// //                 <p>Your verification code is</p>

// //                 <h1 style="
// //                     color:#2196F3;
// //                     letter-spacing:5px;">
// //                 %s
// //                 </h1>

// //                 <p>
// //                     This OTP will expire in
// //                     <b>5 minutes</b>.
// //                 </p>

// //                 <hr>

// //                 <p style="color:gray;">
// //                 If you didn't request this,
// //                 ignore this email.
// //                 </p>

// //                 </div>
// //                 """.formatted(otp);

// //         helper.setText(html, true);

// //         mailSender.send(message);
// //     }

// //     public boolean verifyOtp(String email, String otp) {

// //         OtpDetails details = otpStorage.get(email);

// //         if (details == null)
// //             return false;

// //         if (details.getExpiryTime()
// //                 .isBefore(LocalDateTime.now())) {

// //             otpStorage.remove(email);

// //             return false;
// //         }

// //         if (!details.getOtp().equals(otp))
// //             return false;

// //         otpStorage.remove(email);

// //         return true;
// //     }

// //     private String generateOtp() {

// //         return String.format("%06d",
// //                 random.nextInt(1000000));
// //     }

// //     public void sendPasswordResetEmail(
// //             String email,
// //             String resetLink) throws Exception {

// //         MimeMessage message = mailSender.createMimeMessage();

// //         MimeMessageHelper helper = new MimeMessageHelper(message, true);

// //         helper.setFrom(senderEmail);
// //         helper.setTo(email);
// //         helper.setSubject("Reset Your Password");

// //         helper.setText(
// //                 "Hello,\n\n"
// //                         + "We received a request to reset your password.\n\n"
// //                         + "Click the link below to reset your password:\n\n"
// //                         + resetLink + "\n\n"
// //                         + "This link will expire in 15 minutes.\n\n"
// //                         + "If you did not request a password reset, "
// //                         + "please ignore this email.\n\n"
// //                         + "Regards,\n"
// //                         + "Canteen Verification");

// //         mailSender.send(message);
// //     }

// // }
// package com.example.canteen.mailing;

// import java.net.URI;
// import java.net.http.HttpClient;
// import java.net.http.HttpRequest;
// import java.net.http.HttpResponse;
// import java.security.SecureRandom;
// import java.time.LocalDateTime;
// import java.util.Map;
// import java.util.concurrent.ConcurrentHashMap;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;

// import tools.jackson.databind.ObjectMapper;


// @Service
// public class EmailService {

//     private static final String RESEND_API_URL =
//             "https://api.resend.com/emails";

//     private final HttpClient httpClient;
//     private final ObjectMapper objectMapper;

//     @Value("${resend.api.key}")
//     private String resendApiKey;

//     private final SecureRandom random = new SecureRandom();

//     private final Map<String, OtpDetails> otpStorage =
//             new ConcurrentHashMap<>();

//     // =========================
//     // CONSTRUCTOR
//     // =========================

//     public EmailService() {

//         this.httpClient = HttpClient.newHttpClient();

//         this.objectMapper = new ObjectMapper();
//     }

//     // =========================
//     // SEND OTP
//     // =========================

//     public void sendOtp(String email) throws Exception {

//         String otp = generateOtp();

//         // Store OTP for 5 minutes
//         otpStorage.put(
//                 email,
//                 new OtpDetails(
//                         otp,
//                         LocalDateTime.now().plusMinutes(5)
//                 )
//         );

//         String html = """
//                 <div style="font-family:Arial;padding:20px">

//                 <h2 style="color:#ff9800;">
//                     🍔 Canteen Verification
//                 </h2>

//                 <p>Hello,</p>

//                 <p>Your verification code is</p>

//                 <h1 style="
//                     color:#2196F3;
//                     letter-spacing:5px;">
//                     %s
//                 </h1>

//                 <p>
//                     This OTP will expire in
//                     <b>5 minutes</b>.
//                 </p>

//                 <hr>

//                 <p style="color:gray;">
//                     If you didn't request this,
//                     ignore this email.
//                 </p>

//                 </div>
//                 """.formatted(otp);

//         sendEmail(
//                 email,
//                 "Canteen Email Verification",
//                 html
//         );
//     }

//     // =========================
//     // VERIFY OTP
//     // =========================

//     public boolean verifyOtp(String email, String otp) {

//         OtpDetails details = otpStorage.get(email);

//         if (details == null) {
//             return false;
//         }

//         // Check OTP expiry
//         if (details.getExpiryTime()
//                 .isBefore(LocalDateTime.now())) {

//             otpStorage.remove(email);

//             return false;
//         }

//         // Check OTP
//         if (!details.getOtp().equals(otp)) {
//             return false;
//         }

//         // OTP verified successfully
//         otpStorage.remove(email);

//         return true;
//     }

//     // =========================
//     // GENERATE OTP
//     // =========================

//     private String generateOtp() {

//         return String.format(
//                 "%06d",
//                 random.nextInt(1_000_000)
//         );
//     }

//     // =========================
//     // PASSWORD RESET EMAIL
//     // =========================

//     public void sendPasswordResetEmail(
//             String email,
//             String resetLink) throws Exception {

//         String html = """
//                 <div style="font-family:Arial;padding:20px">

//                 <h2 style="color:#ff9800;">
//                     🍔 Canteen Password Reset
//                 </h2>

//                 <p>Hello,</p>

//                 <p>
//                     We received a request to reset
//                     your password.
//                 </p>

//                 <p>
//                     Click the button below to reset
//                     your password:
//                 </p>

//                 <p>
//                     <a href="%s"
//                        style="
//                        background:#2196F3;
//                        color:white;
//                        padding:12px 20px;
//                        text-decoration:none;
//                        border-radius:5px;
//                        display:inline-block;">
//                        Reset Password
//                     </a>
//                 </p>

//                 <p>
//                     This link will expire in
//                     <b>15 minutes</b>.
//                 </p>

//                 <hr>

//                 <p style="color:gray;">
//                     If you did not request a password reset,
//                     please ignore this email.
//                 </p>

//                 <p>
//                     Regards,<br>
//                     Canteen Verification
//                 </p>

//                 </div>
//                 """.formatted(resetLink);

//         sendEmail(
//                 email,
//                 "Reset Your Password",
//                 html
//         );
//     }

//     // =========================
//     // COMMON RESEND API METHOD
//     // =========================

//     private void sendEmail(
//             String email,
//             String subject,
//             String html) throws Exception {

//         // Create email JSON data
//         Map<String, Object> emailData = Map.of(
//                 "from", "onboarding@resend.dev",
//                 "to", email,
//                 "subject", subject,
//                 "html", html
//         );

//         // Convert Java object to JSON
//         String jsonBody =
//                 objectMapper.writeValueAsString(emailData);

//         // Create HTTP request
//         HttpRequest request =
//                 HttpRequest.newBuilder()
//                         .uri(URI.create(RESEND_API_URL))
//                         .header(
//                                 "Authorization",
//                                 "Bearer " + resendApiKey
//                         )
//                         .header(
//                                 "Content-Type",
//                                 "application/json"
//                         )
//                         .POST(
//                                 HttpRequest.BodyPublishers
//                                         .ofString(jsonBody)
//                         )
//                         .build();

//         // Send request to Resend
//         HttpResponse<String> response =
//                 httpClient.send(
//                         request,
//                         HttpResponse.BodyHandlers.ofString()
//                 );

//         // =========================
//         // SUCCESS
//         // =========================

//         if (response.statusCode() >= 200
//                 && response.statusCode() < 300) {

//             System.out.println(
//                     "Email sent successfully to: "
//                             + email
//             );

//             return;
//         }

//         // =========================
//         // ERROR
//         // =========================

//         System.err.println(
//                 "Resend API Error: "
//                         + response.statusCode()
//         );

//         System.err.println(
//                 "Resend Response: "
//                         + response.body()
//         );

//         throw new RuntimeException(
//                 "Failed to send email. "
//                         + "Resend response: "
//                         + response.body()
//         );
//     }
// }

package com.example.canteen.mailing;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import tools.jackson.databind.ObjectMapper;
@Service
public class EmailService {

    private static final String BREVO_API_URL =
            "https://api.brevo.com/v3/smtp/email";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private final SecureRandom random =
            new SecureRandom();

    private final Map<String, OtpDetails> otpStorage =
            new ConcurrentHashMap<>();

    // =========================
    // CONSTRUCTOR
    // =========================

    public EmailService() {

        this.httpClient = HttpClient.newHttpClient();

        this.objectMapper = new ObjectMapper();
    }

    // =========================
    // SEND OTP
    // =========================

    public void sendOtp(String email) throws Exception {

        String otp = generateOtp();

        String html = """
                <div style="font-family:Arial;padding:20px">

                <h2 style="color:#ff9800;">
                    🍔 Canteen Verification
                </h2>

                <p>Hello,</p>

                <p>Your verification code is:</p>

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

                <p>
                    Regards,<br>
                    <b>Canteen Team</b>
                </p>

                </div>
                """.formatted(otp);

        // Send email first
        sendEmail(
                email,
                "Canteen Email Verification",
                html
        );

        // Store OTP only after successful email
        otpStorage.put(
                email,
                new OtpDetails(
                        otp,
                        LocalDateTime.now().plusMinutes(5)
                )
        );
    }

    // =========================
    // VERIFY OTP
    // =========================

    public boolean verifyOtp(
            String email,
            String otp) {

        OtpDetails details =
                otpStorage.get(email);

        if (details == null) {
            return false;
        }

        // Check expiry
        if (details.getExpiryTime()
                .isBefore(LocalDateTime.now())) {

            otpStorage.remove(email);

            return false;
        }

        // Check OTP
        if (!details.getOtp().equals(otp)) {
            return false;
        }

        // OTP verified
        otpStorage.remove(email);

        return true;
    }

    // =========================
    // GENERATE OTP
    // =========================

    private String generateOtp() {

        return String.format(
                "%06d",
                random.nextInt(1_000_000)
        );
    }

    // =========================
    // PASSWORD RESET EMAIL
    // =========================

    public void sendPasswordResetEmail(
            String email,
            String resetLink) throws Exception {

        String html = """
                <div style="font-family:Arial;padding:20px">

                <h2 style="color:#ff9800;">
                    🍔 Canteen Password Reset
                </h2>

                <p>Hello,</p>

                <p>
                    We received a request to reset
                    your password.
                </p>

                <p>
                    Click the button below to reset
                    your password:
                </p>

                <p>
                    <a href="%s"
                       style="
                       background:#2196F3;
                       color:white;
                       padding:12px 20px;
                       text-decoration:none;
                       border-radius:5px;
                       display:inline-block;">
                       Reset Password
                    </a>
                </p>

                <p>
                    This link will expire in
                    <b>15 minutes</b>.
                </p>

                <hr>

                <p style="color:gray;">
                    If you did not request a password reset,
                    please ignore this email.
                </p>

                <p>
                    Regards,<br>
                    <b>Canteen Team</b>
                </p>

                </div>
                """.formatted(resetLink);

        sendEmail(
                email,
                "Reset Your Canteen Password",
                html
        );
    }

    // =========================
    // COMMON BREVO API METHOD
    // =========================

    private void sendEmail(
            String email,
            String subject,
            String html) throws Exception {

        Map<String, Object> sender =
                Map.of(
                        "name", senderName,
                        "email", senderEmail
                );

        Map<String, Object> recipient =
                Map.of(
                        "email", email
                );

        Map<String, Object> emailData =
                Map.of(
                        "sender", sender,
                        "to", List.of(recipient),
                        "subject", subject,
                        "htmlContent", html
                );

        // Convert Java object to JSON
        String jsonBody =
                objectMapper.writeValueAsString(emailData);

        // Create HTTP request
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(BREVO_API_URL))
                        .header(
                                "accept",
                                "application/json"
                        )
                        .header(
                                "api-key",
                                brevoApiKey
                        )
                        .header(
                                "content-type",
                                "application/json"
                        )
                        .POST(
                                HttpRequest.BodyPublishers
                                        .ofString(jsonBody)
                        )
                        .build();

        // Send request
        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        // =========================
        // SUCCESS
        // =========================

        if (response.statusCode() >= 200
                && response.statusCode() < 300) {

            System.out.println(
                    "Email sent successfully to: "
                            + email
            );

            System.out.println(
                    "Brevo Response: "
                            + response.body()
            );

            return;
        }

        // =========================
        // ERROR
        // =========================

        System.err.println(
                "Brevo API Error: "
                        + response.statusCode()
        );

        System.err.println(
                "Brevo Response: "
                        + response.body()
        );

        throw new RuntimeException(
                "Failed to send email. "
                        + "Brevo response: "
                        + response.body()
        );
    }
}