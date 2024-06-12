package com.esi.mscours.msauth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor @AllArgsConstructor
public class UserContact {

    private String nin;

    @Embedded
    private Contact contact;
}
