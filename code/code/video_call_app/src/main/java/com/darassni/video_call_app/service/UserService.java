package com.darassni.video_call_app.service;

import com.darassni.video_call_app.model.User;
import com.darassni.video_call_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public void register(User user) {
        userRepository.save(user);
        log.info("User with id: {} saved successfully", user.getUserId());
    }

    public User login(User user) {
        User cUser = userRepository.findByEmail(user.getEmail());
        if (cUser == null) {
            throw new RuntimeException("User not found");
        }
        if (!cUser.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("Password incorrect");
        }
        cUser.setStatus("online");
        userRepository.save(cUser);
        log.info("User with id: {} login successfully", user.getUserId());
        return cUser;
    }

    public void logout(String email) {
        User cUser = userRepository.findByEmail(email);
        if (cUser == null) {
            throw new RuntimeException("User not found");
        }
        cUser.setStatus("offline");
        userRepository.save (cUser);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

}
