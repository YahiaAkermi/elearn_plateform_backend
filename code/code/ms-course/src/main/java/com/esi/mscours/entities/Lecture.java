package com.esi.mscours.entities;

import com.esi.mscours.model.Transaction;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Lecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)


    private Long idLecture;

    private Date date;
    private String title;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idGroupe")
    private Groupe groupe;

    @ManyToMany(fetch = FetchType.LAZY)
    private List<Document> documentList;

    @OneToOne
    @JoinColumn(name = "idConference")
    private Conference conference;

    @Transient
    private List<Transaction> payments;

    private int roomId;

    @Override
    public String toString() {
        return "Lecture{" +
                "idLecture=" + idLecture +
                ", date=" + date +
                ", title='" + title + '\'' +
                ", documentList=" + documentList.stream()
                .map(Document::getIdDocument)
                .collect(Collectors.toList()) +


                ", conference=" + (conference != null ? conference.getIdConference() : "null") +
                '}';
    }
}
