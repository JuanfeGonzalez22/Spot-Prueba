package com.spotproject.spot_reservation.repository;

import com.spotproject.spot_reservation.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
}
