package com.esi.mscours.msauth.entities;



import org.springframework.data.rest.core.config.Projection;

@Projection(name = "topayment", types= Auth.class)

public interface UserPaymentProjection {
    Long getId();
    String getFirstName();
    String getLastName();
}

