package com.example.xbankbackend.repositories;

import com.example.xbankbackend.config.JOOQConfig;
import com.example.xbankbackend.generated.tables.Users;
import com.example.xbankbackend.models.User;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class UserRepository {
    private final DSLContext dsl = JOOQConfig.createDSLContext();

    public UserRepository() throws SQLException {
    }

    public void createUser(User user) {
        dsl.insertInto(Users.USERS)
                .values(user.getUserId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getBirthdate())
                .returning()
                .fetchOne()
                .into(User.class);
    }

    public boolean haveEmail(String email) {
        return dsl.selectFrom(Users.USERS)
                .where(Users.USERS.EMAIL.eq(email))
                .fetch()
                .size() == 1;
    }
}
