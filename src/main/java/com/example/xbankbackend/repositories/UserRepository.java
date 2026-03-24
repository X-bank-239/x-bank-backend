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
                .values(user.getUserId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getBirthdate(), user.getPassword(), user.getRole(), user.getActive())
                .execute();
    }

    public User getUser(UUID userId) {
        return dsl.selectFrom(USERS)
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

    public void updatePassword(UUID userId, String hashedPassword) {
        dsl.update(USERS)
                .set(USERS.PASSWORD, hashedPassword)
                .where(USERS.USER_ID.eq(userId))
                .execute();
    }

    public void block(UUID userId) {
        dsl.update(USERS)
                .set(USERS.ACTIVE, false)
                .where(USERS.USER_ID.eq(userId))
                .execute();
    }

    public String getHashedPassword(UUID userId) {
         return dsl.select(USERS.PASSWORD)
                 .from(USERS)
                 .where(USERS.USER_ID.eq(userId))
                 .fetchOne()
                 .into(String.class);
    }
}
