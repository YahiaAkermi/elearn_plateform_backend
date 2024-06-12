package com.esi.mscours.msauth.entities;

import org.springframework.data.rest.core.config.Projection;

@Projection(name = "topayment", types= Teacher.class)

public interface TeacherProjection {
    Long getId();
    String getFirstName();
    String getLastName();
}
