package com.webelement.taskapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
@EnableAsync
@SpringBootApplication
public class TaskappApiApplication {
	
	static boolean isAlpabetic(String s) {
				// "XYZ@GMAIL.COM" // 
		return s.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
	}
	
	public static void main(String[] args) {
		
		SpringApplication.run(TaskappApiApplication.class, args);
		System.err.println(isAlpabetic("xyzgmail.com"));
	}

}
