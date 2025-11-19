package utnfc.isi.back.trabajo.practico.microservicio_logistico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CamionDTO {
    @NotBlank
    private String dominio;

    @NotNull
    private Long idTransportista;

    @NotNull
    @Positive
    private double capacidadPeso;

    @NotNull
    @Positive
    private double capacidadVolumen;

    @NotNull
    @Positive
    private double costoBaseXKm;

    @NotNull
    @Positive
    private double consumoXKm;
}
