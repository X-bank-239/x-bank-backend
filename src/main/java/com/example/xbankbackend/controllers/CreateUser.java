package com.example.xbankbackend.controllers;

import com.example.xbankbackend.models.User;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class CreateUser {

    @PostMapping("/create/user")
    public String newUser(@RequestBody User user) {
        user.setUserId(UUID.randomUUID());
        return user.toString();
    }
}
