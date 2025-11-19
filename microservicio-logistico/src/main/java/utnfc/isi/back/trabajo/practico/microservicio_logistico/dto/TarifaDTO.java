package utnfc.isi.back.trabajo.practico.microservicio_logistico.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TarifaDTO(
    @NotNull
    @Positive
    Double volumenMin,

    @NotNull
    @Positive
    Double volumenMax,
    
    @NotNull
    @Positive
    Double precioCombustible
) {}
