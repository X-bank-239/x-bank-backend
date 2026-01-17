package com.example.xbankbackend.controllers;

import com.example.xbankbackend.dtos.requests.AuthUserRequest;
import com.example.xbankbackend.dtos.requests.CreateUserRequest;
import com.example.xbankbackend.dtos.responses.BankAccountResponse;
import com.example.xbankbackend.dtos.responses.UserProfileResponse;
import com.example.xbankbackend.jwt.JwtUtil;
import com.example.xbankbackend.mappers.UserMapper;
import com.example.xbankbackend.models.User;
import com.example.xbankbackend.services.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/create")
    public ResponseEntity<UserProfileResponse> create(@RequestBody CreateUserRequest userRequest) {
        log.info("Creating user: {}", userRequest);
        User user = userMapper.requestToAccount(userRequest);

        UserProfileResponse createdUser = userService.create(user);
        String token = jwtUtil.generateToken(createdUser.getUserId());


        log.info("Issued JWT for user {}", createdUser.getUserId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Authorization", "Bearer " + token)
                .body(createdUser);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthUserRequest user) {
        log.info("Logging user: {}", user.getEmail());
        if (userService.authenticated(user.getEmail(), user.getPassword())) {
            String token = userService.generateTokenByEmail(user.getEmail());
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .header("Authorization", "Bearer " + token)
                    .build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/get-accounts/{userId}")
    public ResponseEntity<List<BankAccountResponse>> getAccounts(@PathVariable UUID userId) {
        log.info("Getting accounts for user with id {}", userId);
        List<BankAccountResponse> bankAccounts = userService.getAccounts(userId);
        return ResponseEntity.ok(bankAccounts);
    }

    @GetMapping("/get-profile/{userId}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable UUID userId) {
        log.info("Getting user: {}", userId);
        UserProfileResponse userProfile = userService.getProfile(userId);
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping("/get-profile/email/{email}")
    public ResponseEntity<UserProfileResponse> getProfileByEmail(@PathVariable String email) {
        log.info("Getting user: {}", email);
        UserProfileResponse userProfile = userService.getProfileByEmail(email);
        return ResponseEntity.ok(userProfile);
    }
}
