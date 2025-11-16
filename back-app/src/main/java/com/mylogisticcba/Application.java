package com.mylogisticcba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.mylogisticcba.payments.mercadoPago.config.properties.PaymentGatewayProperties;


@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({PaymentGatewayProperties.class})

public class Application {

	public static void main(String[] args) {

        SpringApplication.run(Application.class, args);

	}

}
