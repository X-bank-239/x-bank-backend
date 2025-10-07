package com.example.xbankbackend.controllers;

import com.example.xbankbackend.models.User;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class CreateUser {

    @GetMapping("/create/user")
    public String newUser(@RequestBody User user) {
        return user.getName() + " 1";
    }
}
