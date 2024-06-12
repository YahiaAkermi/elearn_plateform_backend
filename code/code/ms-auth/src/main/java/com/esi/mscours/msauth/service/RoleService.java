package com.esi.mscours.msauth.service;


import com.esi.mscours.msauth.entities.Role;

public interface RoleService {
    Role findByName(String name);
}