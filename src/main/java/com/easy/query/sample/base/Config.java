package com.easy.query.sample.base;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * @author Hoysing
 * @date 2024-07-08 15:21
 * @since 1.0.0
 */
public class Config {
    public static DataSource getDataSource() {
        return getMySqlDataSource();
    }

    public static DataSource getMySqlDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost/eq?useUnicode=true&characterEncoding=UTF-8&useSSL=false&rewriteBatchedStatements=true");
        config.setUsername("root");
        config.setPassword("123456");
        HikariDataSource dataSource = new HikariDataSource(config);
        return dataSource;
    }

    public static DataSource getH2DataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        HikariDataSource dataSource = new HikariDataSource(config);
        return dataSource;
    }
}