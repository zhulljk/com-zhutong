package com.zhut.menu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@MapperScan("com.zhut.menu.mapper")
@EnableDiscoveryClient
public class MenuApplication {

	public static void main(String[] args) {
		SpringApplication.run(MenuApplication.class, args);
	}
}
