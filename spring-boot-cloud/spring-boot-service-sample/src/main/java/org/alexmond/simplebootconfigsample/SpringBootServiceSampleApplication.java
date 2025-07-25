package org.alexmond.simplebootconfigsample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class SpringBootServiceSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootServiceSampleApplication.class, args);
    }

}
