package com.darassni.video_call_app;

import com.darassni.video_call_app.model.User;
import com.darassni.video_call_app.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class VideoCallAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(VideoCallAppApplication.class, args);
	}


	@Bean
	public CommandLineRunner commandLineRunner(UserService userService) {
		return args -> {
			System.out.println("Registering users from CommandLineRunner!");
			userService.register(User.builder()
							.username("user1")
							.email("user1@gmail.com")
							.password("123")
							.status("offline")
					.build());

			userService.register(User.builder()
							.username("user2")
							.email("user2@gmail.com")
							.password("123")
							.status("offline")
					.build());

			userService.register(User.builder()
							.username("user3")
							.email("user3@gmail.com")
							.password("123")
							.status("offline")
					.build());
		};

	}



}
