package com.spotproject.spot_reservation.dto;
import com.spotproject.spot_reservation.model.Spot;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SpotResponseDTO {

    private Long id;
    private String ubicacion;
    private Spot.EstadoSpot estado;

    public static SpotResponseDTO fromEntity(Spot spot) {
        return new SpotResponseDTO(spot.getId(), spot.getUbicacion(), spot.getEstado());
    }
}
