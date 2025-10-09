package org.alexmond.sample.jpa;

import org.alexmond.sample.jpa.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerRest {
    @Autowired
    CustomerRepository customerRepository;

    @GetMapping("/getAllCustomers")
    public String getAllUsers() {
        return customerRepository.findAll().toString();
    }
}
