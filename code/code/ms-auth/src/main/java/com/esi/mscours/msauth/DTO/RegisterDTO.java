package com.esi.mscours.msauth.DTO;

import lombok.Data;

import java.util.Date;

@Data
public class RegisterDTO {
    private String email;
    private String password;
    private Date birthdate;
    private String gender;
    private String firstName;
    private String lastName;

}