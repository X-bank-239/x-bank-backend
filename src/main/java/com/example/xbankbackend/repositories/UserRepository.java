package com.example.xbankbackend.repositories;

import com.example.xbankbackend.models.User;
import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import static com.example.xbankbackend.generated.Tables.USERS;

@AllArgsConstructor
@Repository
public class UserRepository {

    private final DSLContext dsl;

    public void createUser(User user) {
        dsl.insertInto(USERS)
                .values(user.getUserId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getBirthdate())
                .execute();
    }

    public User getUserByUserId(UUID userId) {
        return dsl.selectFrom(USERS)
                .where(USERS.USER_ID.eq(userId))
                .fetchOne()
                .into(User.class);
    }

    public boolean haveEmail(String email) {
        return dsl.selectFrom(USERS)
                .where(USERS.EMAIL.eq(email))
                .fetch()
                .size() == 1;
    }

    public boolean haveUserId(UUID userId) {
        return dsl.selectFrom(USERS)
                .where(USERS.USER_ID.eq(userId))
                .fetch()
                .size() == 1;
    }
}
