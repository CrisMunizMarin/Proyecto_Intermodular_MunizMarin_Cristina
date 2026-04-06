package com.mentorcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync  
public class MentorcoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(MentorcoreApplication.class, args);

	}

}
