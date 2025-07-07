package org.alexmond.simplebootconfigsample.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.alexmond.simplebootconfigsample.config.SupervisorConfig;
import org.alexmond.simplebootconfigsample.model.RunningProcess;
import org.alexmond.simplebootconfigsample.repository.ProcessRepository;
import org.alexmond.simplebootconfigsample.service.ProcessManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Tag(name = "User Services", description = "User API")
@RestController
@RequestMapping("/api/v1")
public class RestProcessController {

    final
    SupervisorConfig supervisorConfig;
    private final AtomicInteger counter = new AtomicInteger();
    private final ProcessRepository processRepository;
    private final ProcessManager processManager;

    @Autowired
    public RestProcessController(ProcessRepository processRepository, SupervisorConfig supervisorConfig , ProcessManager processManager) {
        this.processRepository = processRepository;
        this.supervisorConfig = supervisorConfig;
        this.processManager = processManager;
    }

    @PostMapping("/startAll")
    public void startAllProcess() throws IOException {
        processManager.startAllProcess();
    }


    @GetMapping("/users")
    @Operation(summary = "Returns all users", tags = {"User"})
    @ApiResponse(responseCode = "200", description = "Returns all users",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = RunningProcess.class)))
    public List<RunningProcess> getAllUsers() {
        return processRepository.findAll();
    }

//    @PostMapping("/users")
//    @ResponseStatus(HttpStatus.CREATED)
//    @Operation(summary = "Register a new user")
//    @ApiResponse(responseCode = "201", description = "User successfully created",
//            content = @Content(mediaType = "application/json",
//                    schema = @Schema(implementation = Process.class)))
//    public Process register(@RequestParam(defaultValue = "Stranger") String name) {
//        Process newProcess = new Process(counter.incrementAndGet(), name);
//        return processRepository.addUser(newProcess);
//    }

//    @PutMapping("/users/{id}")
//    @Operation(summary = "Update a user's name")
//    @ApiResponse(responseCode = "200", description = "User successfully updated",
//            content = @Content(mediaType = "application/json",
//                    schema = @Schema(implementation = Process.class)))
//    public Process updateUser(@PathVariable int id, @RequestParam String newName) {
//        return processRepository.updateUser(id, newName)
//                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
//    }
//
//    @DeleteMapping("/users/{id}")
//    @Operation(summary = "Delete a user")
//    @ApiResponse(responseCode = "200", description = "User successfully deleted",
//            content = @Content(mediaType = "application/json",
//                    schema = @Schema(implementation = boolean.class)))
//    public boolean deleteUser(@PathVariable int id) {
//        return processRepository.deleteUser(id);
//    }
}