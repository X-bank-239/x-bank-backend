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

    public void create(User user) {
        dsl.insertInto(USERS)
                .values(user.getUserId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getBirthdate(), user.getPassword())
                .execute();
    }

    public User getUser(UUID userId) {
        return dsl.select(USERS.USER_ID, USERS.EMAIL, USERS.BIRTHDATE, USERS.FIRST_NAME, USERS.LAST_NAME)
                .from(USERS)
                .where(USERS.USER_ID.eq(userId))
                .fetchOne()
                .into(User.class);
    }

    public User getUserByEmail(String email) {
        return dsl.selectFrom(USERS)
                .where(USERS.EMAIL.eq(email))
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

    public String getHashedPassword(UUID userId) {
         return dsl.select(USERS.PASSWORD)
                 .from(USERS)
                .where(USERS.USER_ID.eq(userId))
                .fetch().toString();
    }
}
