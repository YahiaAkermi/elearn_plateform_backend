package com.esi.mscours.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Conference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConference;

    @Column
    private String link;
    @Column
    private String duration;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "idLecture")
    private Lecture lecture;





}
