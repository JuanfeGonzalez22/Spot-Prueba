package com.spotproject.spot_reservation.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservasRequestDTO {

    @NotNull(message = "usuarioId es obligatorio")
    private Long usuarioId;

    @NotNull(message = "horaInicio es obligatoria")
    @Future(message = "horaInicio debe ser una fecha futura")
    private LocalDateTime horaInicio;

    @NotNull(message = "horaFin debe ser obligatoria")
    @Future(message = "horaFin debe ser una fecha futura")
    private LocalDateTime horaFin;

}
