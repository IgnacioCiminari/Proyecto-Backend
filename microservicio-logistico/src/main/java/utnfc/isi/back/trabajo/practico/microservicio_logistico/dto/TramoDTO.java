package utnfc.isi.back.trabajo.practico.microservicio_logistico.dto;

import io.micrometer.common.lang.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TramoDTO(
    @NotNull
    Long tarifaId, 

    @Nullable
    Long origenId, 

    @Nullable
    Long destinoId, 

    @NotNull
    @Positive
    Double distancia, 

    @NotNull
    @Positive
    Integer tiempoEstimado
) {}