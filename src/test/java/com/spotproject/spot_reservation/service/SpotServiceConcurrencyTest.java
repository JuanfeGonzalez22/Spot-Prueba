package com.spotproject.spot_reservation.service;

import com.spotproject.spot_reservation.exception.SpotNoDisponibleException;
import com.spotproject.spot_reservation.model.Reserva;
import com.spotproject.spot_reservation.model.Spot;
import com.spotproject.spot_reservation.model.Usuario;
import com.spotproject.spot_reservation.repository.ReservaRepository;
import com.spotproject.spot_reservation.repository.SpotRepository;
import com.spotproject.spot_reservation.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class SpotServiceConcurrencyTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private SpotService spotService;

    @Autowired
    private SpotRepository spotRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Test
    void reservarSpot_conDiezHilosConcurrentes_soloUnoTieneExito() throws InterruptedException {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombre("Juan")
                .email("juan.concurrencia@test.com")
                .build());

        Spot spot = spotRepository.save(Spot.builder()
                .ubicacion("A1")
                .estado(Spot.EstadoSpot.DISPONIBLE)
                .build());

        int numeroDeHilos = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numeroDeHilos);
        CountDownLatch hilosListos = new CountDownLatch(numeroDeHilos);
        CountDownLatch senialDeSalida = new CountDownLatch(1);
        CountDownLatch hilosTerminados = new CountDownLatch(numeroDeHilos);

        AtomicInteger exitos = new AtomicInteger();
        AtomicInteger rechazosPorSpotNoDisponible = new AtomicInteger();
        List<Throwable> excepcionesInesperadas = Collections.synchronizedList(new ArrayList<>());

        LocalDateTime horaInicio = LocalDateTime.now();
        LocalDateTime horaFin = horaInicio.plusHours(1);

        for (int i = 0; i < numeroDeHilos; i++) {
            executor.submit(() -> {
                try {
                    hilosListos.countDown();
                    senialDeSalida.await();
                    spotService.reservarSpot(spot.getId(), usuario.getId(), horaInicio, horaFin);
                    exitos.incrementAndGet();
                } catch (SpotNoDisponibleException e) {
                    rechazosPorSpotNoDisponible.incrementAndGet();
                } catch (Throwable t) {
                    excepcionesInesperadas.add(t);
                } finally {
                    hilosTerminados.countDown();
                }
            });
        }

        hilosListos.await();
        senialDeSalida.countDown();
        boolean terminaronATiempo = hilosTerminados.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(terminaronATiempo).isTrue();
        assertThat(excepcionesInesperadas).isEmpty();
        assertThat(exitos.get()).isEqualTo(1);
        assertThat(rechazosPorSpotNoDisponible.get()).isEqualTo(numeroDeHilos - 1);

        List<Reserva> reservasDelSpot = reservaRepository.findAll().stream()
                .filter(r -> r.getSpot().getId().equals(spot.getId()))
                .toList();
        assertThat(reservasDelSpot).hasSize(1);
    }
}
