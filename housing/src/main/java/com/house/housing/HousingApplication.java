package com.house.housing;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HousingApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(HousingApplication.class);
        application.setBannerMode(Banner.Mode.OFF);
        application.run(args);
    }

}
