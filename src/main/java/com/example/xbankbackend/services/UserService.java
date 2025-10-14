package com.example.xbankbackend.services;

import com.example.xbankbackend.models.User;
import com.example.xbankbackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public void createUser(User user) {
        if (!userRepository.haveEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists");
        }
        user.setUserId(UUID.randomUUID());
        userRepository.createUser(user);
    }
}
