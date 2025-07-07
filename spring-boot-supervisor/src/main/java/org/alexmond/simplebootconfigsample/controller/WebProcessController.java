package org.alexmond.simplebootconfigsample.controller;

import org.alexmond.simplebootconfigsample.model.ProcessStatusRest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/process")
public class WebProcessController {

    @GetMapping
    public String listUsers(Model model) {
        List<ProcessStatusRest> processes = userService.getAllUsers();
        model.addAttribute("users", processes);
        model.addAttribute("title", "Process List");
        return "process/list";
    }
}
