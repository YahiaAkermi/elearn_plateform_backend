package com.esi.mscours.msauth.dao;

import com.esi.mscours.msauth.entities.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminDao extends JpaRepository<Admin,Long> {
    Admin findAdminByEmail(String email);
}
