package com.esi.mscours.msauth.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Teacher {

    @Id
    private  Long id;
    private  Long userId;
    @Column(unique=true)
    private String email ;

    private String firstName;
    private String lastName;
    @Temporal(TemporalType.DATE)
    private Date birthdate;
    private  String gender;
    private boolean status;

    private String moduleName;
    private String cv; // Add this field for the CV link
    private Long idWallet;

}
