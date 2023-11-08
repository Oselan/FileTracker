package com.oselan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling 
public class FileTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileTrackerApplication.class, args);
	}

}
