package com.esi.mscours.repository;

import com.esi.mscours.entities.Groupe;
import com.esi.mscours.entities.GroupeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupeRepository extends JpaRepository<Groupe, Long> {

    List<Groupe> findGroupesByIdTeacher(Long idTeacher);

    // Custom query for searching groupes
  /*  @Query("SELECT g FROM Groupe g " +
            "JOIN Module m ON g.module.idModule = m.idModule " +
            "JOIN Teacher t ON g.idTeacher = t.id " +
            "WHERE (:name IS NULL OR g.name LIKE %:name%) " +
            "AND (:firstName IS NULL OR t.firstName LIKE %:firstName%) " +
            "AND (:lastName IS NULL OR t.lastName LIKE %:lastName%) " +
            "AND (:moduleName IS NULL OR m.name LIKE %:moduleName%)")
    List<Groupe> searchGroupes(@Param("name") String name,
                               @Param("firstName") String firstName,
                               @Param("lastName") String lastName,
                               @Param("moduleName") String moduleName);


   */
    List<Groupe> findGroupesByStatus(GroupeStatus status);
    Groupe findGroupeByIdGroupe(Long idGroupe);
}
