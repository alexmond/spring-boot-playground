package org.alexmond.sample.jpa.data;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(indexes = {
    @Index(name = "idx_customer_first_name", columnList = "firstName"),
    @Index(name = "idx_customer_last_name", columnList = "lastName")
})
public class Customer {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    @OneToMany(mappedBy = "customerId")
    private List<Roles> roles;
}