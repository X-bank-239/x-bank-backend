package com.example.xbankbackend.services.user;

import com.example.xbankbackend.exceptions.UserAlreadyExistsException;
import com.example.xbankbackend.exceptions.UserGivesIncorrectEmail;
import com.example.xbankbackend.exceptions.UserNotFoundException;
import com.example.xbankbackend.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

@AllArgsConstructor
@Service
public class UserValidationService {

    private static final int MIN_USER_AGE = 12;
    private static final int MAX_USER_AGE = 100;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private UserRepository userRepository;

    public void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new UserGivesIncorrectEmail(email + " is not a valid email address");
        }
    }

    public void validateAge(Date userBirthdate) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -MIN_USER_AGE);
        Date minDate = cal.getTime();

        if (userBirthdate.after(minDate)) {
            throw new IllegalArgumentException("Birthdate " + userBirthdate + " is too young");
        }

        cal.add(Calendar.YEAR, -(MAX_USER_AGE - MIN_USER_AGE));
        Date maxDate = cal.getTime();

        if (userBirthdate.before(maxDate)) {
            throw new IllegalArgumentException("Birthdate " + userBirthdate + " is too old");
        }
    }

    public void validateUserExists(UUID userId) {
        if (!userRepository.exists(userId)) {
            throw new UserNotFoundException("User with UUID " + userId + " already exists");
        }
    }

    public void validateUserExistsByEmail(String email) {
        validateEmail(email);
        if (!userRepository.existsByEmail(email)) {
            throw new UserNotFoundException("User with email " + email + " already exists");
        }
    }

    public void validateEmailIsUnique(String email) {
        validateEmail(email);
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }
    }
}
