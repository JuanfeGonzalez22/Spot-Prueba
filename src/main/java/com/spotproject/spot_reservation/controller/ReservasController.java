package com.spotproject.spot_reservation.controller;

import com.spotproject.spot_reservation.model.Reserva;
import com.spotproject.spot_reservation.model.Spot;
import com.spotproject.spot_reservation.repository.ReservaRepository;
import com.spotproject.spot_reservation.repository.SpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservasController {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private SpotRepository spotRepository;

    @GetMapping
    public List<Reserva> getReservas(){
        return reservaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> verReserva(@PathVariable Long id){
        return reservaRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmarReserva(@PathVariable Long id){
        return reservaRepository.findById(id).map(reserva -> {
            reserva.setEstado(Reserva.EstadoReserva.CONFIRMED);
            reservaRepository.save(reserva);
            return ResponseEntity.ok(reserva);
        })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarReserva(@PathVariable Long id){
        return reservaRepository.findById(id).map(reserva -> {
            reserva.setEstado(Reserva.EstadoReserva.CANCELLED);
            reservaRepository.save(reserva);

            Spot spot = reserva.getSpot();
            spot.setEstado(Spot.EstadoSpot.DISPONIBLE);;
            spotRepository.save(spot);

            return ResponseEntity.ok(reserva);
        })
                .orElse(ResponseEntity.notFound().build());
    }
}

