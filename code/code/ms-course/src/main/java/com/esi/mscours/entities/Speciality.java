package com.esi.mscours.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Speciality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSpeciality;
    @Enumerated(EnumType.STRING)
    private SpecialityName name;

    @OneToMany(mappedBy = "speciality", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Module> modules;

}