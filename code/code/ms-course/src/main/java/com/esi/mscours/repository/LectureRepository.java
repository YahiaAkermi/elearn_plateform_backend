package com.esi.mscours.repository;

import com.esi.mscours.entities.Groupe;
import com.esi.mscours.entities.Lecture;
import com.esi.mscours.entities.Module;
import com.esi.mscours.entities.ModuleName;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@RepositoryRestResource
    public interface LectureRepository extends JpaRepository<Lecture,Long> {
    List<Lecture> findLectureByGroupe(Groupe groupe);
    Lecture findLectureByIdLecture(Long idLecture);


    }
