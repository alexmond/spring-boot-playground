package org.alexmond.sample.cloud;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAdminServer
public class SpringBootAdminSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootAdminSampleApplication.class, args);
    }

}
