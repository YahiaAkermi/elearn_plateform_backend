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
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idModule;
    @Enumerated(EnumType.STRING)
    private ModuleName name;


    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Groupe> groupes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idSpeciality")
    private Speciality speciality;


}
