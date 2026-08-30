package org.alexmond.batch.restart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.batch.autoconfigure.BatchAutoConfiguration;

/**
 * {@code BatchAutoConfiguration} is excluded because {@link PersistentBatchConfig}
 * declares its own {@code jobRepository}.
 *
 * <p>Without the exclusion the context fails to start with
 * {@code BeanDefinitionOverrideException} on bean {@code jobRepository}: Boot's
 * {@code SpringBootBatchDefaultConfiguration} declares one too, and Boot forbids
 * silent overriding. Setting {@code spring.main.allow-bean-definition-overriding}
 * would also start, but it resolves the clash by letting whichever definition
 * happens to be registered last win — the wrong fix for a bean whose identity is
 * the entire subject of this module.
 */
@SpringBootApplication(exclude = BatchAutoConfiguration.class)
public class BatchRestartApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchRestartApplication.class, args);
    }
}
