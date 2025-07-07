package org.alexmond.simplebootconfigsample;

import org.alexmond.simplebootconfigsample.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerRest {
    @Autowired
    CustomerRepository customerRepository;
    @GetMapping("/getAllCustomers")
    public String getAllUsers(){
        return customerRepository.findAll().toString();
    }
}
