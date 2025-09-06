package org.alexmond.sample.jpa.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.alexmond.sample.jpa.data.Roles;
import org.alexmond.sample.jpa.repository.RolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@io.swagger.v3.oas.annotations.tags.Tag(name = "Roles", description = "Endpoints for managing roles")
public class RolesRest {
    @Autowired
    RolesRepository rolesRepository;

    @Operation(
            summary = "Get all roles",
            description = "Return all roles from the database",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Roles returned successfully",
                            content = @Content(schema = @Schema(type = "array", implementation = Roles.class)))
            }
    )
    @GetMapping("/getAllRoles")
    public ResponseEntity<List<Roles>> getAllRoles(){
        List<Roles> gotRoles = rolesRepository.findAll();
        return ResponseEntity.ok(gotRoles);
    }

    @Operation(
            summary = "Get a role by its ID",
            description = "Return the role associated with the specified ID",
            parameters = {
                    @Parameter(name = "id", description = "The id associated with the desired role", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Role returned successfully",
                            content = @Content(schema = @Schema(implementation = Roles.class))),
                    @ApiResponse(responseCode = "404", description = "Role with the specified id not found")
            }
    )
    @GetMapping("/getRoleByID/{id}")
    public ResponseEntity<Roles> getRoleByID(@PathVariable("id") Long id){
        Roles gotRole = rolesRepository.findById(id).orElse(null);
        return ResponseEntity.ok(gotRole);
    }

    @Operation(
            summary = "Add a role",
            description = "Creates a role with a predefined ID",
            parameters = {
                    @Parameter(name = "roleName", description = "Name of the role", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Role created successfully",
                            content = @Content(schema = @Schema(implementation = Roles.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @PostMapping("/addRole")
    public ResponseEntity<Roles> addRole(@RequestBody Roles role) {
        rolesRepository.save(role);
        return ResponseEntity.ok(role);
    }

    @Operation(
            summary = "Add a role",
            description = "Creates a role with the specified ID",
            parameters = {
                    @Parameter(name = "id", description = "ID for the role", required = true),
                    @Parameter(name = "roleName", description = "Name of the role", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Role created successfully",
                            content = @Content(schema = @Schema(implementation = Roles.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @PostMapping("/addRoleByID/{id}/{roleName}")
    public ResponseEntity<Roles> addRole(@PathVariable("id") Long id, @PathVariable("roleName") String roleName) {
        Roles addedRole = new Roles();
        addedRole.setId(id);
        addedRole.setRoleName(roleName);
        rolesRepository.save(addedRole);
        return ResponseEntity.ok(addedRole);
    }

    @Operation(
            summary = "Remove a role",
            description = "Remove an existing role based on the provided ID",
            parameters = {
                    @Parameter(name = "id", description = "ID of the role to remove", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Role removed successfully",
                            content = @Content(schema = @Schema(implementation = Roles.class))),
                    @ApiResponse(responseCode = "404", description = "Role not found")
            }
    )
    @DeleteMapping("/removeRole/{id}")
    public ResponseEntity<Roles> removeRole(@PathVariable("id") Long id) {
        if (!rolesRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Roles removedRole = rolesRepository.findById(id).orElse(null);
        rolesRepository.deleteById(id);
        return ResponseEntity.ok(removedRole);
    }
}
