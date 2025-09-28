package org.alexmond.sample.thymeleaf.auth.service;

import org.alexmond.sample.thymeleaf.auth.model.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    
    private final List<Product> products = Arrays.asList(
        new Product(1L, "Laptop", "High-performance laptop", new BigDecimal("999.99"), 
                10, "Electronics", Arrays.asList("computer", "portable", "work"), 
                LocalDateTime.now().minusDays(5), true, "/images/laptop.jpg"),
        
        new Product(2L, "Coffee Maker", "Programmable coffee maker", new BigDecimal("79.99"), 
                25, "Appliances", Arrays.asList("coffee", "kitchen", "morning"), 
                LocalDateTime.now().minusDays(3), false, "/images/coffee-maker.jpg"),
        
        new Product(3L, "Running Shoes", "Comfortable running shoes", new BigDecimal("129.99"), 
                15, "Sports", Arrays.asList("running", "fitness", "comfortable"), 
                LocalDateTime.now().minusDays(1), true, "/images/shoes.jpg"),
        
        new Product(4L, "Smartphone", "Latest smartphone model", new BigDecimal("699.99"), 
                8, "Electronics", Arrays.asList("phone", "mobile", "communication"), 
                LocalDateTime.now().minusDays(7), true, "/images/phone.jpg"),
        
        new Product(5L, "Cookbook", "Delicious recipes collection", new BigDecimal("24.99"), 
                30, "Books", Arrays.asList("cooking", "recipes", "food"), 
                LocalDateTime.now().minusDays(2), false, "/images/cookbook.jpg")
    );
    
    public List<Product> getProducts(String category, String search) {
        return products.stream()
                .filter(product -> category == null || product.getCategory().equalsIgnoreCase(category))
                .filter(product -> search == null || 
                        product.getName().toLowerCase().contains(search.toLowerCase()) ||
                        product.getDescription().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    public List<Product> getFeaturedProducts() {
        return products.stream()
                .filter(Product::isFeatured)
                .collect(Collectors.toList());
    }
    
    public List<String> getAllCategories() {
        return products.stream()
                .map(Product::getCategory)
                .distinct()
                .collect(Collectors.toList());
    }
}
