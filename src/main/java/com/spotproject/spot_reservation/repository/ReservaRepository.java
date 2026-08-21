package com.spotproject.spot_reservation.repository;

import com.spotproject.spot_reservation.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query("SELECT r FROM Reserva r WHERE r.estado = 'PENDING' AND r.creadaEn < :limite")
    List<Reserva> findPendientesVencidas(@Param("limite") LocalDateTime limite);

}
