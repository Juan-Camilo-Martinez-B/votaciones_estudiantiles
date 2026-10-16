package com.universidad.votaciones;

import com.universidad.votaciones.voting.VotacionApi;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class VotacionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(VotacionesApplication.class, args);
    }

    @Bean
    public CommandLineRunner inicializarDatosElectorales(VotacionApi votacionApi) {
        return args -> {
            if (votacionApi.listarCandidatos().isEmpty()) {
                votacionApi.registrarCandidato(
                        "candidato-1",
                        "01",
                        "Carlos Mendoza",
                        "Modernización tecnológica y bienestar estudiantil"
                );
                votacionApi.registrarCandidato(
                        "candidato-2",
                        "02",
                        "Laura Restrepo",
                        "Transparencia y participación activa universitaria"
                );
                votacionApi.registrarCandidato(
                        "candidato-blanco",
                        "99",
                        "Voto en Blanco",
                        "Opción democrática institucional"
                );
            }
        };
    }
}
