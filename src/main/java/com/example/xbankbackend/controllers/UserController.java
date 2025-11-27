package com.example.xbankbackend.controllers;

import com.example.xbankbackend.dtos.UserProfileDTO;
import com.example.xbankbackend.models.User;
import com.example.xbankbackend.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Log4j2
@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    private ObjectMapper objectMapper;

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody User user) {
        log.info("Creating user: {}", user);
        userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/get-profile/{userId}")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable UUID userId) {
        log.info("Getting user: {}", userId);
        UserProfileDTO userProfile = userService.getProfile(userId);
        return ResponseEntity.ok(userProfile);
    }
}
