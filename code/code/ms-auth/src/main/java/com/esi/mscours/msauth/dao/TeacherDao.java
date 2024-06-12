package com.esi.mscours.msauth.dao;

import com.esi.mscours.msauth.entities.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface TeacherDao extends JpaRepository<Teacher, Long> {
    Boolean existsByEmail(String email);
    Teacher findTeacherByEmail(String email);
    Teacher findTeacherById(Long id);
}
