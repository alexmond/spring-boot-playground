package org.alexmond.sample.auth.controller;

import org.alexmond.sample.auth.model.Product;
import org.alexmond.sample.auth.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping
    public String listProducts(Model model, 
                             @RequestParam(required = false) String category,
                             @RequestParam(required = false) String search) {
        List<Product> products = productService.getProducts(category, search);
        model.addAttribute("products", products);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("searchTerm", search);
        model.addAttribute("title", "Product Catalog");
        return "products/list";
    }
    
    @GetMapping("/featured")
    public String featuredProducts(Model model) {
        List<Product> products = productService.getFeaturedProducts();
        model.addAttribute("products", products);
        model.addAttribute("title", "Featured Products");
        return "products/featured";
    }
}
