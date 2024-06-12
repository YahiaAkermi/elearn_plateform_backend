package com.esi.mscours.msauth.dao;

import com.esi.mscours.msauth.entities.Gender;
import com.esi.mscours.msauth.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentDao extends JpaRepository<Student,Long> {

    List<Student> findStudentsByGender(Gender gender );

    Student findStudentByEmail(String email);
    Boolean existsByEmail(String email);

    Student findStudentById(Long id);



}
