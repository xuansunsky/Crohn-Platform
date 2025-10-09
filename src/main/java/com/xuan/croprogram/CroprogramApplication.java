package com.xuan.croprogram;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
@SpringBootApplication
public class CroprogramApplication {

    public static void main(String[] args) {
        SpringApplication.run(CroprogramApplication.class, args);
    }


}
@Configuration // 1. 告诉Spring：这是一个“配置类”，请优先加载我
class SecurityConfig {

    @Bean // 2. 告诉Spring：请执行这个方法，并把它返回的对象，作为一个Bean，放进你的容器里
    public PasswordEncoder passwordEncoder() {
        // 3. 我们在这里，亲手new了这个对象
        return new BCryptPasswordEncoder();
    }
}
