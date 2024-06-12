package com.esi.mscours.msauth.DTO;

import lombok.Data;

import java.util.Date;
@Data
public class StudentDTO {
    private  String email;
    private String password;

    private String firstName;
    private String lastName;
    private Date birthdate;
    private  String gender;
    private String status;
    private String speciality;

}
