package com.example.xbankbackend.repositories;

import com.example.xbankbackend.config.JOOQConfig;
import com.example.xbankbackend.generated.tables.Users;
import com.example.xbankbackend.models.User;
import org.jooq.DSLContext;
import org.jooq.Name;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.UUID;

@Repository
public class UserRepository {
    private final DSLContext dsl = JOOQConfig.createDSLContext();

    public UserRepository() throws SQLException {
    }

    public void createUser(User user) {
        dsl.insertInto(Users.USERS)
                .values(user.getUserId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getBirthdate())
                .execute();
    }

    public boolean haveEmail(String email) {
        System.out.println(dsl.selectFrom(Users.USERS).fetch());
        return dsl.selectFrom(Users.USERS)
                .where(Users.USERS.EMAIL.eq(email))
                .fetch()
                .size() == 1;
    }

    public boolean haveUUID(UUID uuid) {
        return dsl.selectFrom(Users.USERS)
                .where(Users.USERS.USER_ID.eq(uuid))
                .fetch()
                .size() == 1;
    }
}
