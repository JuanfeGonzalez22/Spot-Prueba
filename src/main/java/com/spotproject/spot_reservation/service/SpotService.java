package com.spotproject.spot_reservation.service;

import com.spotproject.spot_reservation.exception.RecursosNoEncontradosException;
import com.spotproject.spot_reservation.exception.SpotNoDisponibleException;
import com.spotproject.spot_reservation.model.Reserva;
import com.spotproject.spot_reservation.model.Spot;
import com.spotproject.spot_reservation.model.Usuario;
import com.spotproject.spot_reservation.repository.ReservaRepository;
import com.spotproject.spot_reservation.repository.SpotRepository;
import com.spotproject.spot_reservation.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SpotService {

    @Autowired
    private SpotRepository spotRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Transactional
    public Reserva reservarSpot(Long spotId, Long usuarioId, LocalDateTime horaInicio, LocalDateTime horaFin) {

        Spot spot = spotRepository.findByIdWithLock(spotId)
                .orElseThrow(() -> new RecursosNoEncontradosException("Spot not found"));

        if (spot.getEstado() != Spot.EstadoSpot.DISPONIBLE) {
            throw new SpotNoDisponibleException("Spot no disponible");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursosNoEncontradosException("Usuario dosent exist"));

        spot.setEstado(Spot.EstadoSpot.RESERVADO);
        spotRepository.save(spot);

        Reserva reserva = Reserva.builder()
                .spot(spot)
                .usuario(usuario)
                .horaInicio(horaInicio)
                .horaFin(horaFin)
                .estado(Reserva.EstadoReserva.PENDING)
                .creadaEn(LocalDateTime.now())
                .build();

        return reservaRepository.save(reserva);
    }

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void expirarReservaVencidas() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(15);
        List<Reserva> vencidas = reservaRepository.findPendientesVencidas(limite);

        for (Reserva reserva : vencidas) {
            reserva.setEstado(Reserva.EstadoReserva.EXPIRED);
            reservaRepository.save(reserva);

            Spot spot = reserva.getSpot();
            spot.setEstado(Spot.EstadoSpot.DISPONIBLE);
            spotRepository.save(spot);

            System.out.printf("Reserva " + reserva.getId() + " expirada, spot " + spot.getId() + " liberado.");
        }
    }
}
