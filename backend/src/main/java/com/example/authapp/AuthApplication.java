package com.example.authapp;

import com.example.authapp.config.AdminSeedProperties;
import com.example.authapp.config.AppSecurityProperties;
import com.example.authapp.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, AppSecurityProperties.class, AdminSeedProperties.class})
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
