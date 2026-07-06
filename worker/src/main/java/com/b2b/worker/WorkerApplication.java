package com.b2b.worker;

import com.b2b.worker.domain.service.TaxCalculationService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkerApplication.class, args);
	}

    @Bean
    TaxCalculationService taxCalculationService(){
        return new TaxCalculationService();
    }

}
