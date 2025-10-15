package com.example.xbankbackend.controllers;

import com.example.xbankbackend.models.User;
import com.example.xbankbackend.services.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@CrossOrigin
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/create/user")
    public String newUser(@RequestBody User user) {
        try {
            log.info(user);
            userService.createUser(user);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
        }
        return user.toString();
    }
}
