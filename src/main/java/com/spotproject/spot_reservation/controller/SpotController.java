package com.spotproject.spot_reservation.controller;

import com.spotproject.spot_reservation.model.Reserva;
import com.spotproject.spot_reservation.model.Spot;
import com.spotproject.spot_reservation.repository.SpotRepository;
import com.spotproject.spot_reservation.service.SpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/spots")
public class SpotController {

    @Autowired
    private SpotRepository spotRepository;

    @Autowired
    private SpotService spotService;

    @GetMapping
    public List<Spot> listarSpots(){
        return spotRepository.findAll();
    }

    @PostMapping
    public Spot crearSpot(@RequestBody Spot spot){
        spot.setEstado(Spot.EstadoSpot.DISPONIBLE);
        return spotRepository.save(spot);
    }

    @PostMapping("/{spotId}/reservar")
    public ResponseEntity<?> reservarSpot(
            @PathVariable Long spotId,
            @RequestParam Long usuarioId,
            @RequestParam String horaInicio,
            @RequestParam String horaFin) {

        try {
            Reserva reserva = spotService.reservarSpot(
                    spotId,
                    usuarioId,
                    LocalDateTime.parse(horaInicio),
                    LocalDateTime.parse(horaFin)
            );
            return ResponseEntity.ok(reserva);
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }


}
