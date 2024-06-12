package com.esi.mscours.msauth.service;


import com.esi.mscours.msauth.DTO.AuthDto;
import com.esi.mscours.msauth.entities.Auth;

import java.util.List;

public interface UserService {
    Auth save(AuthDto user);
    List<Auth> findAll();
    Auth findOne(String nin);
}