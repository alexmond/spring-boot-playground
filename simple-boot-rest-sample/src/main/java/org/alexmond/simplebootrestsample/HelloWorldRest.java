package org.alexmond.simplebootrestsample;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldRest {
    @GetMapping("/hello")
    public String HelloWorld(){
        return "Hello World";
    }
}
