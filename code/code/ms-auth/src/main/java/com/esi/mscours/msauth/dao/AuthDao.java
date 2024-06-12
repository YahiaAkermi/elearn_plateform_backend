package com.esi.mscours.msauth.dao;


import com.esi.mscours.msauth.entities.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthDao extends JpaRepository<Auth, Long> {
    Auth findByEmail(String email);
    Boolean existsByEmail(String email);
    Auth findAuthByEmail(String email);
}