package com.esi.mscours.msauth.DTO;

import lombok.Data;

import java.util.Date;
@Data
public class TeacherDTO {
    private  String email;
    private String firstName;
    private String password;
    private String lastName;
    private Date birthdate;
    private  String gender;
    private String status;
    private String moduleName;
    private String cv; // Add this field for the CV link

}

