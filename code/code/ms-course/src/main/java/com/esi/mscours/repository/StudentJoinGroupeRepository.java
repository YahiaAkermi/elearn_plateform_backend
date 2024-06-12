package com.esi.mscours.repository;

import com.esi.mscours.entities.StudentJoinGroupe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


import java.util.List;
@RepositoryRestResource
public interface StudentJoinGroupeRepository extends JpaRepository<StudentJoinGroupe, Long> {

    List<StudentJoinGroupe> findStudentJoinGroupesByIdStudent (Long idStudent);
    List<StudentJoinGroupe> findStudentJoinGroupesByIdGroupe (Long idGroupe);
    StudentJoinGroupe findStudentJoinGroupeByIdGroupeAndIdStudent(Long idGroupe,Long idStudent);

}
