package com.universidad.votaciones.votacion.infraestructura.salida.jpa;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * El paquete JPA es el adaptador de PostgreSQL.
 * Entidades y repositorios de esta carpeta solo existen con ese perfil.
 */
@Configuration
@Profile("postgres")
@EntityScan(basePackageClasses = JornadaJpa.class)
@EnableJpaRepositories(basePackageClasses = JornadaJpaRepo.class)
class PostgresJpaConfig {
}
