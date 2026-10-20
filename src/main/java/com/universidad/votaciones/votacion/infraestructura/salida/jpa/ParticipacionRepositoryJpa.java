package com.universidad.votaciones.votacion.infraestructura.salida.jpa;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.universidad.votaciones.votacion.aplicacion.puertos.salida.ParticipacionRepository;
import com.universidad.votaciones.votacion.dominio.Participacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Component
@Profile("postgres")
public class ParticipacionRepositoryJpa implements ParticipacionRepository {

    private final ParticipacionJpaRepo repo;

    ParticipacionRepositoryJpa(ParticipacionJpaRepo repo) {
        this.repo = repo;
    }

    @Override
    public Optional<Participacion> buscarPorLlave(String llaveIdempotencia) {
        if (llaveIdempotencia == null || llaveIdempotencia.isBlank()) {
            return Optional.empty();
        }
        return repo.findById(llaveIdempotencia.trim()).map(fila -> Participacion.reconstruir(
                fila.llaveIdempotencia, fila.idEstudiante, fila.tokenAnonimo, fila.fechaHoraPeticion));
    }

    @Override
    public boolean yaVoto(String idEstudiante) {
        return idEstudiante != null && !idEstudiante.isBlank() && repo.existsByIdEstudiante(idEstudiante.trim());
    }

    @Override
    public void guardar(Participacion participacion) {
        ParticipacionJpa fila = new ParticipacionJpa();
        fila.llaveIdempotencia = participacion.llaveIdempotencia();
        fila.idEstudiante = participacion.idEstudiante();
        fila.tokenAnonimo = participacion.tokenRecibo();
        fila.fechaHoraPeticion = participacion.fechaHora();
        fila.estado = "PROCESADO";
        repo.save(fila);
    }
}

@Entity
@Table(name = "transacciones_voto_idempotente")
class ParticipacionJpa {
    @Id
    String llaveIdempotencia;
    @Column(nullable = false)
    String idEstudiante;
    String tokenAnonimo;
    @Column(nullable = false)
    LocalDateTime fechaHoraPeticion;
    @Column(nullable = false)
    String estado;

    protected ParticipacionJpa() {
    }
}

interface ParticipacionJpaRepo extends JpaRepository<ParticipacionJpa, String> {
    boolean existsByIdEstudiante(String idEstudiante);
}
