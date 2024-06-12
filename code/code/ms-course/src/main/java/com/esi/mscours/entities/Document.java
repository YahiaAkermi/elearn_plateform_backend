package com.esi.mscours.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity

@Builder
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDocument;
    private String link;
    private String name;
    private String type;
    private Long idTeacher;

    @ManyToMany(mappedBy = "documentList")
    @JsonIgnore
    private List<Lecture> lecturesList;
}
