package br.udesc.casaemdia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CasaEmDiaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CasaEmDiaApplication.class, args);
    }
}
