package com.example.xbankbackend.repositories;

import com.example.xbankbackend.models.User;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import static com.example.xbankbackend.generated.Tables.USERS;
@Log4j2
@AllArgsConstructor
@Repository
public class UserRepository {

    private final DSLContext dsl;

    public Optional<User> create(User user) {
            dsl.insertInto(USERS)
                .values(user.getUserId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getBirthdate())
                .execute();
            return Optional.ofNullable(this.getUser(user.getUserId()));
    }

    public User getUser(UUID userId) {
        return dsl.selectFrom(USERS)
                .where(USERS.USER_ID.eq(userId))
                .fetchOne()
                .into(User.class);
    }

    public boolean existsByEmail(String email) {
        return dsl.selectFrom(USERS)
                .where(USERS.EMAIL.eq(email))
                .fetch()
                .size() == 1;
    }

    public boolean exists(UUID userId) {
        return dsl.selectFrom(USERS)
                .where(USERS.USER_ID.eq(userId))
                .fetch()
                .size() == 1;
    }
}
