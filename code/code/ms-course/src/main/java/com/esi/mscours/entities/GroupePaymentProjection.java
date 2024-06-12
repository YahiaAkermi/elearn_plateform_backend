package com.esi.mscours.entities;


import org.springframework.data.rest.core.config.Projection;

@Projection(name = "topayment", types= Groupe.class)

public interface GroupePaymentProjection {
    Long getIdGroupe();

    String getName();
}
