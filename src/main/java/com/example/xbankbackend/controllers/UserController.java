package com.example.xbankbackend.controllers;

import com.example.xbankbackend.dtos.requests.CreateUserRequest;
import com.example.xbankbackend.dtos.responses.UserProfileResponse;
import com.example.xbankbackend.mappers.UserMapper;
import com.example.xbankbackend.models.User;
import com.example.xbankbackend.services.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Log4j2
@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {

    private UserService userService;
    private UserMapper userMapper;

    @PostMapping("/create")
    public ResponseEntity<User> create(@RequestBody CreateUserRequest userRequest) {
        log.info("Creating user: {}", userRequest);
        User user = userMapper.requestToAccount(userRequest);
        User createdUser = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/get-profile/{userId}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable UUID userId) {
        log.info("Getting user: {}", userId);
        UserProfileResponse userProfile = userService.getProfile(userId);
        return ResponseEntity.ok(userProfile);
    }
}
