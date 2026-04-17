package com.example.xbankbackend.controllers;

import com.example.xbankbackend.dtos.requests.AuthUserRequest;
import com.example.xbankbackend.dtos.requests.CreateUserRequest;
import com.example.xbankbackend.dtos.requests.UpdatePasswordRequest;
import com.example.xbankbackend.dtos.requests.Verify2FARequest;
import com.example.xbankbackend.dtos.responses.BankAccountResponse;
import com.example.xbankbackend.dtos.TempAuthState;
import com.example.xbankbackend.dtos.responses.LoginInitResponse;
import com.example.xbankbackend.dtos.responses.UserProfileResponse;
import com.example.xbankbackend.jwt.JwtUtil;
import com.example.xbankbackend.mappers.UserMapper;
import com.example.xbankbackend.models.AuthResponse;
import com.example.xbankbackend.models.User;
import com.example.xbankbackend.services.user.UserService;
import com.example.xbankbackend.services.VerificationService;
import com.example.xbankbackend.services.external.notification.EmailSender;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Log4j2
@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {

    private UserService userService;
    private UserMapper userMapper;
    private JwtUtil jwtUtil;
    private VerificationService verificationService;
    private EmailSender emailSender;

    @PostMapping("/create")
    public ResponseEntity<UserProfileResponse> create(@RequestBody CreateUserRequest userRequest) {
        log.info("Creating user: {}", userRequest);
        User user = userMapper.requestToAccount(userRequest);

        UserProfileResponse createdUser = userService.create(user);
        String token = jwtUtil.generateToken(createdUser.getUserId(), createdUser.getRole().toString());


        log.info("Issued JWT for user {}", createdUser.getUserId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginInitResponse> login(@RequestBody AuthUserRequest request) {
        log.info("Login attempt for user: {}", request.getEmail());
        if (!userService.authenticated(request.getEmail(), request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserProfileResponse user = userService.getProfileByEmail(request.getEmail());
        TempAuthState state = emailSender.sendVerificationCode(user.getUserId(), user.getEmail());
        String tempToken = jwtUtil.generateTempToken(state.getId(), user.getEmail());

        LoginInitResponse response = new LoginInitResponse();
        response.setRequires2fa(true);
        response.setTempToken(tempToken);
        response.setEmail(user.getEmail());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/login/2fa")
    public ResponseEntity<AuthResponse> verify2FA(@RequestBody Verify2FARequest request) {
        UUID stateId = jwtUtil.extractStateId(request.getTempToken());
        String email = jwtUtil.extractEmail(request.getTempToken());
        log.info("2fa for user with stateId {} and email {}", stateId, email);

        if (!verificationService.verifyCode(stateId, request.getCode())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = userService.generateTokenByEmail(email);

        AuthResponse response = new AuthResponse();
        response.setToken(token);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        log.info("Getting user: {}", userId);
        UserProfileResponse userProfile = userService.getProfile(userId);
        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> updateCurrentUserProfile(@RequestBody UpdatePasswordRequest passwordRequest, Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        log.info("Updating password for user: {}", userId);
        System.out.println(passwordRequest);
        userService.changePassword(userId, passwordRequest.getOldPassword(), passwordRequest.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    // ADMIN-only

    @GetMapping("/get-accounts/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BankAccountResponse>> getAccounts(@PathVariable UUID userId) {
        log.info("Getting accounts for user with id {}", userId);
        List<BankAccountResponse> bankAccounts = userService.getAccounts(userId);
        return ResponseEntity.ok(bankAccounts);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> blockUser(@PathVariable UUID userId) {
        log.info("Blocking user with id {}", userId);
        userService.blockUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable UUID userId) {
        log.info("Getting user: {}", userId);
        UserProfileResponse userProfile = userService.getProfile(userId);
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> getProfileByEmail(@PathVariable String email, Authentication auth) {
        log.info("Getting user: {}", email);
        UserProfileResponse userProfile = userService.getProfileByEmail(email);
        return ResponseEntity.ok(userProfile);
    }
}
