package io.github.whitedg.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("io.github.whitedg.demo")
public class MybatisCryptoDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MybatisCryptoDemoApplication.class, args);
    }

}
