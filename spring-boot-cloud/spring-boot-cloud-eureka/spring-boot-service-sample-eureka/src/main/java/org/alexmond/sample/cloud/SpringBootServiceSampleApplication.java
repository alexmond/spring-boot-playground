package org.alexmond.sample.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
@EnableDiscoveryClient
public class SpringBootServiceSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootServiceSampleApplication.class, args);
    }

}
