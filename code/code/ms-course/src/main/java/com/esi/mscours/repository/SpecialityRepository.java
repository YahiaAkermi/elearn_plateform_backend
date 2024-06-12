package com.esi.mscours.repository;


import com.esi.mscours.DTO.SpecialityDTO;
import com.esi.mscours.entities.Speciality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;

@RepositoryRestResource
public interface SpecialityRepository extends JpaRepository<Speciality, Long> {
    @Query("SELECT new com.esi.mscours.DTO.SpecialityDTO( s.idSpeciality,s.name) FROM Speciality s")
    List<SpecialityDTO> findAllSpecialities();
}
