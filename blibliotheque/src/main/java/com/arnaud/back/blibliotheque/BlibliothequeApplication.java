package com.arnaud.back.blibliotheque;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDate;

@SpringBootApplication
@EnableSwagger2
@EnableBatchProcessing
public class 	BlibliothequeApplication {


	public static void main(String[] args) {
		SpringApplication.run(BlibliothequeApplication.class, args);
	}

}
