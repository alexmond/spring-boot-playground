package org.alexmond.sample.jpa.repository;

import org.alexmond.sample.jpa.data.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
