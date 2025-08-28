package org.alexmond.sample.jpa.repository;

// java
import java.util.List;

import org.alexmond.sample.jpa.data.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByFirstName(String firstName);
    List<Customer> findByLastName(String lastName);
}