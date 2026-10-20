package com.universidad.votaciones.votacion.dominio;

import java.util.Map;

public record Escrutinio(long totalVotos, Map<String, Long> votosPorCandidato) {
}
