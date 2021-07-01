package com.atguigu.gmall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class GamllSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(GamllSearchApplication.class, args);
    }

}
