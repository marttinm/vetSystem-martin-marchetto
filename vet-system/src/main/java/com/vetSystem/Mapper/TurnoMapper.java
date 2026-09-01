package com.vetSystem.Mapper;

import com.vetSystem.Dto.TurnoResponseDTO;
import com.vetSystem.Entity.Turno;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TurnoMapper {

    @Mapping(source = "mascota.id", target = "mascotaId")
    @Mapping(source = "mascota.nombre", target = "mascotaNombre")
    @Mapping(source = "veterinario.id", target = "veterinarioId")
    @Mapping(source = "veterinario.nombre", target = "veterinarioNombre")
    TurnoResponseDTO toDTO(Turno turno);

    List<TurnoResponseDTO> toDTOList(List<Turno> turnos);
}
