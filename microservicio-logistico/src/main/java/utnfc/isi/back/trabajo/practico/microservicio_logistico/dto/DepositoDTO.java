package utnfc.isi.back.trabajo.practico.microservicio_logistico.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DepositoDTO(
    @NotBlank
    String nombre,

    @NotBlank
    String direccion,

    @NotNull 
    @DecimalMin(value = "-90.0", inclusive = true)
    @DecimalMax(value = "90.0", inclusive = true)
    Double latitud,

    @NotNull
    @DecimalMin(value = "-180.0", inclusive = true)
    @DecimalMax(value = "180.0", inclusive = true)
    Double longitud,

    @NotNull
    @Positive
    Double costoEstadiaDiario
) {}
