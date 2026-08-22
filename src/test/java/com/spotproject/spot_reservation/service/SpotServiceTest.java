package com.spotproject.spot_reservation.service;

import com.spotproject.spot_reservation.exception.RecursosNoEncontradosException;
import com.spotproject.spot_reservation.exception.SpotNoDisponibleException;
import com.spotproject.spot_reservation.model.Reserva;
import com.spotproject.spot_reservation.model.Spot;
import com.spotproject.spot_reservation.model.Usuario;
import com.spotproject.spot_reservation.repository.ReservaRepository;
import com.spotproject.spot_reservation.repository.SpotRepository;
import com.spotproject.spot_reservation.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotServiceTest {

    @Mock
    private SpotRepository spotRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private SpotService spotService;

    private static final Long SPOT_ID = 1L;
    private static final Long USUARIO_ID = 2L;

    private Spot spotDisponible() {
        return Spot.builder()
                .id(SPOT_ID)
                .ubicacion("A1")
                .estado(Spot.EstadoSpot.DISPONIBLE)
                .build();
    }

    private Usuario usuario() {
        return Usuario.builder()
                .id(USUARIO_ID)
                .nombre("Juan")
                .email("juan@test.com")
                .build();
    }

    @Test
    void reservarSpot_reservaExitosa_cuandoSpotEstaDisponible() {
        Spot spot = spotDisponible();
        Usuario usuario = usuario();
        LocalDateTime horaInicio = LocalDateTime.now();
        LocalDateTime horaFin = horaInicio.plusHours(1);

        when(spotRepository.findByIdWithLock(SPOT_ID)).thenReturn(Optional.of(spot));
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reserva resultado = spotService.reservarSpot(SPOT_ID, USUARIO_ID, horaInicio, horaFin);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getEstado()).isEqualTo(Reserva.EstadoReserva.PENDING);
        assertThat(resultado.getSpot()).isEqualTo(spot);
        assertThat(resultado.getUsuario()).isEqualTo(usuario);
        assertThat(resultado.getHoraInicio()).isEqualTo(horaInicio);
        assertThat(resultado.getHoraFin()).isEqualTo(horaFin);
        assertThat(spot.getEstado()).isEqualTo(Spot.EstadoSpot.RESERVADO);

        verify(spotRepository).save(spot);

        ArgumentCaptor<Reserva> reservaCaptor = ArgumentCaptor.forClass(Reserva.class);
        verify(reservaRepository).save(reservaCaptor.capture());
        assertThat(reservaCaptor.getValue().getCreadaEn()).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(value = Spot.EstadoSpot.class, names = {"RESERVADO", "OCUPADO"})
    void reservarSpot_lanzaSpotNoDisponible_cuandoSpotNoEstaDisponible(Spot.EstadoSpot estadoNoDisponible) {
        Spot spot = Spot.builder()
                .id(SPOT_ID)
                .ubicacion("A1")
                .estado(estadoNoDisponible)
                .build();
        LocalDateTime horaInicio = LocalDateTime.now();
        LocalDateTime horaFin = horaInicio.plusHours(1);

        when(spotRepository.findByIdWithLock(SPOT_ID)).thenReturn(Optional.of(spot));

        assertThatThrownBy(() -> spotService.reservarSpot(SPOT_ID, USUARIO_ID, horaInicio, horaFin))
                .isInstanceOf(SpotNoDisponibleException.class);

        verify(usuarioRepository, never()).findById(anyLong());
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void reservarSpot_lanzaRecursosNoEncontrados_cuandoSpotNoExiste() {
        LocalDateTime horaInicio = LocalDateTime.now();
        LocalDateTime horaFin = horaInicio.plusHours(1);

        when(spotRepository.findByIdWithLock(SPOT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> spotService.reservarSpot(SPOT_ID, USUARIO_ID, horaInicio, horaFin))
                .isInstanceOf(RecursosNoEncontradosException.class);

        verify(usuarioRepository, never()).findById(anyLong());
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void reservarSpot_lanzaRecursosNoEncontrados_cuandoUsuarioNoExiste() {
        Spot spot = spotDisponible();
        LocalDateTime horaInicio = LocalDateTime.now();
        LocalDateTime horaFin = horaInicio.plusHours(1);

        when(spotRepository.findByIdWithLock(SPOT_ID)).thenReturn(Optional.of(spot));
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> spotService.reservarSpot(SPOT_ID, USUARIO_ID, horaInicio, horaFin))
                .isInstanceOf(RecursosNoEncontradosException.class);

        verify(spotRepository, never()).save(any());
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void reservarSpot_lanzaSpotNoDisponible_cuandoHoraFinNoEsPosteriorAHoraInicio() {
        Spot spot = spotDisponible();
        LocalDateTime horaInicio = LocalDateTime.now();
        LocalDateTime horaFin = horaInicio.minusMinutes(1);

        when(spotRepository.findByIdWithLock(SPOT_ID)).thenReturn(Optional.of(spot));

        assertThatThrownBy(() -> spotService.reservarSpot(SPOT_ID, USUARIO_ID, horaInicio, horaFin))
                .isInstanceOf(SpotNoDisponibleException.class);

        verify(usuarioRepository, never()).findById(anyLong());
        verify(spotRepository, never()).save(any());
        verify(reservaRepository, never()).save(any());
    }
}
