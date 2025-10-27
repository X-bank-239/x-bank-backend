package com.example.xbankbackend.config;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Bean;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JOOQConfig {
    @Bean
    public DSLContext createDSLContext() throws SQLException {
        String username = "postgres";
        String password = "mysecretpassword";
        String url = "jdbc:postgresql://localhost:5432/postgres";
        Connection connection = DriverManager.getConnection(url, username, password);
        DSLContext context = DSL.using(connection, SQLDialect.POSTGRES);
        return context;
    }
}
