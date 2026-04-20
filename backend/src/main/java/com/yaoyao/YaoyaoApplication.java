package com.yaoyao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yaoyao.mapper")
public class YaoyaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(YaoyaoApplication.class, args);
    }
}
