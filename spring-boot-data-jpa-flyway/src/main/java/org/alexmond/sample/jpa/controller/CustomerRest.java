package org.alexmond.sample.jpa.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.alexmond.sample.jpa.data.Customer;
import org.alexmond.sample.jpa.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class CustomerRest {
    @Autowired
    CustomerRepository customerRepository;

    @Operation(
            summary = "Get all customers",
            description = "Return all customers from the database",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Customers returned successfully",
                            content = @Content(schema = @Schema(type = "array", implementation = Customer.class)))
            }
    )
    @GetMapping("/getAllCustomers")
    public ResponseEntity<List<Customer>> getAllUsers(){
        List<Customer> gotCustomers = customerRepository.findAll();
        return ResponseEntity.ok(gotCustomers);
    }

    @Operation(
            summary = "Get a customer by their ID",
            description = "Return the customer with the specified ID",
            parameters = {
                    @Parameter(name = "id", description = "The id of the desired customer", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Customer returned successfully",
                            content = @Content(schema = @Schema(implementation = Customer.class))),
                    @ApiResponse(responseCode = "204", description = "Customer with the specified id not found")
            }
    )
    @GetMapping("/getCustomerByID/{id}")
    public ResponseEntity<Customer> getCustomerByID(@PathVariable("id") Long id){
        Customer gotCustomer = customerRepository.findById(id).orElse(null);
        return ResponseEntity.ok(gotCustomer);
    }

    @Operation(
            summary = "Get customers by their first name",
            description = "Return all customers who have the specified first name",
            parameters = {
                    @Parameter(name = "firstName", description = "The first name of the desired customers",
                            required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Customers returned successfully",
                            content = @Content(schema = @Schema(type = "array", implementation = Customer.class))),
                    @ApiResponse(responseCode = "204",
                            description = "Customer(s) with the specified first name not found")
            }
    )
    @GetMapping("/getCustomersByFirstName/{firstName}")
    public ResponseEntity<List<Customer>> getCustomersByFirstName(@PathVariable("firstName") String firstName){
        List<Customer> gotCustomers = customerRepository.findByFirstName(firstName);
        if (gotCustomers.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(gotCustomers);
    }

    @Operation(
            summary = "Get customers by their last name",
            description = "Return all customers who have the specified last name",
            parameters = {
                    @Parameter(name = "lastName", description = "The last name of the desired customers",
                            required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Customers returned successfully",
                            content = @Content(schema = @Schema(type = "array", implementation = Customer.class))),
                    @ApiResponse(responseCode = "204",
                            description = "Customer(s) with the specified last name not found")
            }
    )
    @GetMapping("/getCustomersByLastName/{lastName}")
    public ResponseEntity<List<Customer>> getCustomersByLastName(@PathVariable("lastName") String lastName){
        List<Customer> gotCustomers = customerRepository.findByLastName(lastName);
        if (gotCustomers.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(gotCustomers);
    }

    @Operation(
            summary = "Add a customer",
            description = "Creates a single customer with predefined data",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Customer created successfully",
                            content = @Content(schema = @Schema(implementation = Customer.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @PostMapping("/addCustomer")
    public ResponseEntity<Customer> addCustomer(){
        Customer addedCustomer = addCustomerWithDetails("firstname", "lastname", "firstname_lastname@gmail.com");
        return ResponseEntity.ok(addedCustomer);
    }

    @Operation(
            summary = "Add multiple customers",
            description = "Creates the specified number of customers with generated data",
            parameters = {
                    @Parameter(name = "num", description = "The number of customers to add", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "201", description = "Customers created successfully",
                            content = @Content(schema = @Schema(type = "array", implementation = Customer.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid number parameter")
            }
    )
    @PostMapping("/addMultipleCustomers/{num}")
    public ResponseEntity<List<Customer>> addMultipleCustomers(@PathVariable("num") int num){
        List<Customer> addedCustomers = new ArrayList<>();
        for (int i = 1; i <= num; i++) {
            Customer addedCustomer = addCustomerWithDetails("firstname" + i,
                    "lastname" + i, "firstname_lastname" + i + "@gmail.com");
            addedCustomers.add(addedCustomer);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(addedCustomers);
    }

    private Customer addCustomerWithDetails(String firstName, String lastName, String email){
        Customer customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customerRepository.save(customer);
        return customer;
    }

    @Operation(
            summary = "Remove a customer",
            description = "Remove an existing customer based on the provided ID",
            parameters = {
                    @Parameter(name = "id", description = "ID of the customer to remove", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Customer removed successfully",
                            content = @Content(schema = @Schema(implementation = Customer.class))),
                    @ApiResponse(responseCode = "404", description = "Customer not found")
            }
    )
    @DeleteMapping("/removeCustomer/{id}")
    public ResponseEntity<Customer> removeCustomer(@PathVariable("id") Long id) {
        if (!customerRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Customer removedCustomer = customerRepository.findById(id).orElse(null);
        customerRepository.deleteById(id);
        return ResponseEntity.ok(removedCustomer);
    }

    @Operation(
            summary = "Edit customer details",
            description = "Updates an existing customer's information based on the provided ID and request body",
            parameters = {
                    @Parameter(name = "id", description = "ID of the customer to update", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Customer updated successfully",
                            content = @Content(schema = @Schema(implementation = Customer.class))),
                    @ApiResponse(responseCode = "404", description = "Customer not found")
            }
    )
    @PutMapping("/editCustomer/{id}")
    public ResponseEntity<Customer> editCustomer(@PathVariable("id") Long id, @RequestBody Customer customer) {
        if (!customerRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Customer existingCustomer = customerRepository.getReferenceById(id);
        existingCustomer.setFirstName(customer.getFirstName());
        existingCustomer.setLastName(customer.getLastName());
        existingCustomer.setEmail(customer.getEmail());
        Customer updatedCustomer = customerRepository.save(existingCustomer);
        return ResponseEntity.ok(updatedCustomer);
    }
}