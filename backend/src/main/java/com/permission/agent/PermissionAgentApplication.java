package com.permission.agent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.permission.agent.mapper")
@EnableScheduling
public class PermissionAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PermissionAgentApplication.class, args);
    }
}
