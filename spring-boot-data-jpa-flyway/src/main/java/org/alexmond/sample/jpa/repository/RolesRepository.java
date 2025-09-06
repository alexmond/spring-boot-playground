package org.alexmond.sample.jpa.repository;

import org.alexmond.sample.jpa.data.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolesRepository extends JpaRepository<Roles, Long> {
}