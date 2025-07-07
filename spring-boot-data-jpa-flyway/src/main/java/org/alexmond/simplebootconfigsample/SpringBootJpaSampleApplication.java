package org.alexmond.simplebootconfigsample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EnableJpaRepositories(basePackages = "org.alexmond.simplebootconfigsample.repository")
public class SpringBootJpaSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootJpaSampleApplication.class, args);
    }

}
