package org.alexmond.sample.jpa.controller;

import org.alexmond.sample.jpa.data.Customer;
import org.alexmond.sample.jpa.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerRest {
    @Autowired
    CustomerRepository customerRepository;

    @GetMapping("/getAllCustomers")
    public String getAllUsers(){
        return customerRepository.findAll().toString();
    }

    @PostMapping("/addCustomer")
    public String addCustomer(){
        Customer customer = addCustomerWithDetails("firstname", "lastname", "firstname_lastname@gmail.com");
        return customer.toString();
    }

    @PostMapping("/addMultipleCustomers")
    public String addMultipleCustomers(){
        int n = 200;
        for (int i = 1; i <= n; i++) {
            addCustomerWithDetails("firstname" + i, "lastname" + i, "firstname_lastname" + i + "@gmail.com");
        }
        return "Added " + n + " customers";
    }

    private Customer addCustomerWithDetails(String firstName, String lastName, String email){
        Customer customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customerRepository.save(customer);
        return customer;
    }
}
