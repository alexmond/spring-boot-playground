package org.alexmond.simplebootconfigsample.repository;

import org.alexmond.simplebootconfigsample.data.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
