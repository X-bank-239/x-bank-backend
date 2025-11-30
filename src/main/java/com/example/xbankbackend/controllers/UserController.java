package com.example.xbankbackend.controllers;

import com.example.xbankbackend.dtos.requests.CreateUserRequest;
import com.example.xbankbackend.dtos.responses.UserProfileResponse;
import com.example.xbankbackend.models.User;
import com.example.xbankbackend.services.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@AllArgsConstructor
@Log4j2
@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {

    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<User> create(@Valid @RequestBody CreateUserRequest userRequest) {
        log.info("Creating user: {}", userRequest);

        User user = convertRequest(userRequest);
        userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/get-profile/{userId}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable UUID userId) {
        log.info("Getting user: {}", userId);
        UserProfileResponse userProfile = userService.getProfile(userId);
        return ResponseEntity.ok(userProfile);
    }

    private User convertRequest(CreateUserRequest userRequest) {
        User user = new User();

        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setBirthdate(userRequest.getBirthdate());

        return user;
    }
}
