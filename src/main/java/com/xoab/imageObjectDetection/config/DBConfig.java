package com.xoab.imageObjectDetection.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DBConfig {
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.driver-class-name}")
    private String driver;

    @Bean(name = "getDataSource")
    public DataSource getDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();

        dataSourceBuilder.driverClassName(driver);
        dataSourceBuilder.username(username);
        dataSourceBuilder.password(password);
        dataSourceBuilder.url(url);

        return dataSourceBuilder.build();
    }

    @Bean(name = "getJdbcTemplate")
    @Primary
    public JdbcTemplate getJdbcTemplate(@Qualifier("getDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager getTransactionManager(@Qualifier("getDataSource") DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }
}
