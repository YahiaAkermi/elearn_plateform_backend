package com.esi.mscours.msauth.entities;


import org.springframework.data.rest.core.config.Projection;

@Projection(name = "topayment", types= Student.class)

public interface StudentProjection {
    Long getId();
    String getFirstName();
    String getLastName();
}
