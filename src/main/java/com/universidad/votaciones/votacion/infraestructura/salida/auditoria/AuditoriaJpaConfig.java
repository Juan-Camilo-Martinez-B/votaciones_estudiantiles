package com.universidad.votaciones.votacion.infraestructura.salida.auditoria;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackageClasses = EventoAuditoria.class)
@EnableJpaRepositories(basePackageClasses = EventoAuditoriaRepository.class)
class AuditoriaJpaConfig {
}
