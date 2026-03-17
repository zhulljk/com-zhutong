package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.mybatis.spring.annotation.MapperScan;

import com.example.config.AppConfig;

@SpringBootApplication(scanBasePackages = "com.example")
@MapperScan("com.example.mapper")
@EnableConfigurationProperties({
    AppConfig.JwtProperties.class,
    AppConfig.MailProperties.class,
    AppConfig.SmsProperties.class
})
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
