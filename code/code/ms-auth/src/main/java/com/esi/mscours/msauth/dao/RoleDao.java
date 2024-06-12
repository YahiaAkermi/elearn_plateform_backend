package com.esi.mscours.msauth.dao;


import com.esi.mscours.msauth.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleDao extends JpaRepository<Role, Long> {
    Role findRoleByName(String name);
    Role findByName(String name);
}