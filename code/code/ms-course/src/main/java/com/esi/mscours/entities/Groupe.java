package com.esi.mscours.entities;

import com.esi.mscours.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Groupe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idGroupe;

    private String name;
    private Double lecturePrice;
    private int max;
    private String lectureDay;
    private int initialLecturesNumber;
    private int minMustPayLecturesNumber;
    private GroupeStatus status;
    private String image;

    @OneToMany(mappedBy = "groupe", fetch = FetchType.LAZY)
    private List<Lecture> lectures;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idModule")
    private Module module;

    private Long idTeacher;
    @Transient
    private User teacher;

    @OneToMany
    private List<StudentJoinGroupe> students;
}
