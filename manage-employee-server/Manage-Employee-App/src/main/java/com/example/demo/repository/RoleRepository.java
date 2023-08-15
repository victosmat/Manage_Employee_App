package com.example.demo.repository;

import com.example.demo.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoleRepository extends JpaRepository<Role, Long> {
    @Query("SELECT new Role(r.id, r.noteRole) FROM Role r WHERE r.noteRole = ?1")
    Role findByNoteRole(Role.NoteRole noteRole);
}
