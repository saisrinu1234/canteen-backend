package com.example.canteen.mailing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/public")
@CrossOrigin(origins = "*")
public class MailingController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse> sendOtp(
            @RequestBody OtpRequest request) {

        try {

            emailService.sendOtp(request.getEmail());

            return ResponseEntity.ok(
                    new ApiResponse(
                            true,
                            "OTP sent successfully"));

        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(
                    new ApiResponse(
                            false,
                            "Unable to send OTP"));
        }

    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(
            @RequestBody VerifyOtpRequest request) {

        boolean success =
                emailService.verifyOtp(
                        request.getEmail(),
                        request.getOtp());

        if (success) {

            return ResponseEntity.ok(
                    new ApiResponse(
                            true,
                            "OTP Verified"));

        }

        return ResponseEntity.badRequest().body(
                new ApiResponse(
                        false,
                        "Invalid or Expired OTP"));

    }

}