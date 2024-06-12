package com.esi.mscours.msauth.service.impl;

import com.esi.mscours.msauth.DTO.AuthDto;
import com.esi.mscours.msauth.dao.AuthDao;
import com.esi.mscours.msauth.entities.Auth;
import com.esi.mscours.msauth.entities.Role;
import com.esi.mscours.msauth.service.RoleService;
import com.esi.mscours.msauth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service(value = "userService")
public class UserServiceImpl implements UserDetailsService, UserService {

    @Autowired
    private RoleService roleService;

    @Autowired
    private AuthDao userDao;

    @Autowired
    BCryptPasswordEncoder bcryptEncoder;

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Auth auth = userDao.findByEmail(email);
        if(auth == null){
            throw new UsernameNotFoundException("Invalid email or password.");
        }
        return new org.springframework.security.core.userdetails.User(auth.getEmail(), auth.getPassword(), getAuthority(auth));
    }

    private Set<SimpleGrantedAuthority> getAuthority(Auth auth) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        auth.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        });
        return authorities;
    }

    public List<Auth> findAll() {
        List<Auth> list = new ArrayList<>();
        userDao.findAll().iterator().forEachRemaining(list::add);
        return list;
    }

    @Override
    public Auth findOne(String email) {
        return userDao.findByEmail(email);
    }

    @Override
    public Auth save(AuthDto user) {

        Auth nAuth = user.getUserFromDto();
        nAuth.setPassword(bcryptEncoder.encode(user.getPassword()));

        Role role = roleService.findByName("USER");
        List<Role> roleSet = new ArrayList<>();
        roleSet.add(role);
        nAuth.setRoles(roleSet);
        return userDao.save(nAuth);
    }

}
