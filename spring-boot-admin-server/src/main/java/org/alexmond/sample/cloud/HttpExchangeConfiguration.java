package org.alexmond.sample.cloud;

import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpExchangeConfiguration {

    @Bean
    public InMemoryHttpExchangeRepository createTraceRepository() {
        InMemoryHttpExchangeRepository inMemoryHttpExchangeRepository = new InMemoryHttpExchangeRepository();
        inMemoryHttpExchangeRepository.setCapacity(200);
        return  inMemoryHttpExchangeRepository;
    }
}