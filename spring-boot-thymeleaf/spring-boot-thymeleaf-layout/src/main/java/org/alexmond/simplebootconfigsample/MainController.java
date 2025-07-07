package org.alexmond.simplebootconfigsample;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(Model model) {
        // Add sample products to demonstrate the fix
        List<Product> products = createSampleProducts();
        model.addAttribute("products", products);
        model.addAttribute("title", "Home");
        model.addAttribute("content", "pages/home");
        return "layout";
    }

    @PostMapping("/")
    public String flashMsg(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("success", "This is a flash message!");
        return "redirect:/";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About");
        model.addAttribute("content", "pages/about");
        return "layout";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("title", "Contact");
        model.addAttribute("content", "pages/contact");
        return "layout";
    }

    // Helper method to create sample products
    private List<Product> createSampleProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("Product 1", new BigDecimal("299.99")));
        products.add(new Product("Product 2", new BigDecimal("599.99")));
        products.add(new Product("Product 3", new BigDecimal("149.99")));
        products.add(new Product("Product 4", null)); // Product with null price
        return products;
    }

    // Simple Product class
    public static class Product {
        private String name;
        private BigDecimal price;

        public Product(String name, BigDecimal price) {
            this.name = name;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}