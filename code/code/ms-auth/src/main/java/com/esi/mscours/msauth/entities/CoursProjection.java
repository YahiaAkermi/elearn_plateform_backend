package com.esi.mscours.msauth.entities;




import org.springframework.data.rest.core.config.Projection;


@Projection(name = "tocours", types= Teacher.class)
public interface CoursProjection {
    String getId();
    String getFirstName();

    String getLastName();

    String getEmail();

}