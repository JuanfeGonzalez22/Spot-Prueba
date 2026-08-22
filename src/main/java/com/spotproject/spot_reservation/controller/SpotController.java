package com.spotproject.spot_reservation.controller;

import com.spotproject.spot_reservation.dto.ReservasRequestDTO;
import com.spotproject.spot_reservation.dto.SpotRequestDTO;
import com.spotproject.spot_reservation.dto.SpotResponseDTO;
import com.spotproject.spot_reservation.model.Reserva;
import com.spotproject.spot_reservation.model.Spot;
import com.spotproject.spot_reservation.repository.SpotRepository;
import com.spotproject.spot_reservation.service.SpotService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/spots")
public class SpotController {

    @Autowired
    private SpotRepository spotRepository;

    @Autowired
    private SpotService spotService;

    @GetMapping
    public List<SpotResponseDTO> listarSpots(){
        return spotRepository.findAll().stream()
                .map(SpotResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @PostMapping
    public SpotResponseDTO crearSpot(@Valid @RequestBody SpotRequestDTO request) {
        Spot spot = Spot.builder()
                .ubicacion(request.getUbicacion())
                .estado(Spot.EstadoSpot.DISPONIBLE)
                .build();
        Spot guardado = spotRepository.save(spot);
        return SpotResponseDTO.fromEntity(guardado);
    }

    @PostMapping("/{spotId}/reservar")
    public ResponseEntity<?> reservarSpot(
            @PathVariable Long spotId,
            @Valid @RequestBody ReservasRequestDTO request) {

       Reserva reserva = spotService.reservarSpot(
               spotId,
               request.getUsuarioId(),
               request.getHoraInicio(),
               request.getHoraFin()
       );
       return ResponseEntity.ok(reserva);
    }


}
