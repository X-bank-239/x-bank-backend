package com.example.xbankbackend.repositories;

import com.example.xbankbackend.models.User;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.UUID;

import static com.example.xbankbackend.generated.Tables.USERS;

@Repository
public class UserRepository {
    private final DSLContext dsl;

    @Autowired
    public UserRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void createUser(User user) {
        dsl.insertInto(USERS)
                .values(user.getUserId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getBirthdate())
                .execute();
    }

    public User getUser(UUID uuid) {
        return Objects.requireNonNull(
                dsl.selectFrom(USERS)
                        .where(USERS.USER_ID.eq(uuid))
                        .fetchOne()
                )
                .into(User.class);
    }

    public boolean haveEmail(String email) {
        return dsl.selectFrom(USERS)
                .where(USERS.EMAIL.eq(email))
                .fetch()
                .size() == 1;
    }

    public boolean haveUUID(UUID uuid) {
        return dsl.selectFrom(USERS)
                .where(USERS.USER_ID.eq(uuid))
                .fetch()
                .size() == 1;
    }
}
