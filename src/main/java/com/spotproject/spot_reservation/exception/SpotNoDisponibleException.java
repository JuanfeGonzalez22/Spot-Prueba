package com.spotproject.spot_reservation.exception;

public class SpotNoDisponibleException extends RuntimeException {
    public SpotNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
