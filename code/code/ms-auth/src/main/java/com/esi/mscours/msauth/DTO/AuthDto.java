package com.esi.mscours.msauth.DTO;

import com.esi.mscours.msauth.entities.Auth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthDto {
    
    private String email;
    private String password;



    public Auth getUserFromDto(){
        Auth auth = new Auth();
        auth.setEmail(email);
        auth.setPassword(password);
        return auth;
    }
    
}