package com.medicalstore.pos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class MedicalStorePosApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicalStorePosApplication.class, args);
    }
}





