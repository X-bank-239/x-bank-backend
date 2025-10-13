package com.example.xbankbackend.controllers;

import com.example.xbankbackend.models.User;
import com.example.xbankbackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class CreateUser {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create/user")
    public String newUser(@RequestBody User user) {
        System.out.println(user);
        user.setUserId(UUID.randomUUID());
        userRepository.createUser(user);
        return user.toString();
    }
}
