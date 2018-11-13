package com.whl.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @program: esspringboot
 * @description:
 * @author: Mr.Wang
 * @create: 2018-11-07 17:57
 **/
@SpringBootApplication
@ComponentScan(basePackages = { "com.whl.demo" })
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
