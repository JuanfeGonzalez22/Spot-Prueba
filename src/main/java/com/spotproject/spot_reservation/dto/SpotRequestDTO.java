package com.spotproject.spot_reservation.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SpotRequestDTO {
    @NotBlank(message = "ubicacion es obligatoria")
    private String ubicacion;
}
