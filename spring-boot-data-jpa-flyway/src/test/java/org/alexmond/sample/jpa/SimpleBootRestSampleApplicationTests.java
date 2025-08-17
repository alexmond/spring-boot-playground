package org.alexmond.sample.jpa;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SimpleBootRestSampleApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    private Flyway flyway;

    @BeforeEach
    void clearDatabase() {
        flyway.clean();
        flyway.migrate();
    }

}
