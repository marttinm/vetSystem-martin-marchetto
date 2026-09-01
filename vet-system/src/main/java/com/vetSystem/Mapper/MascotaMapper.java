package com.vetSystem.Mapper;

import com.vetSystem.Dto.MascotaDTO;
import com.vetSystem.Entity.Mascota;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MascotaMapper {

    // El duenio se aplana: del objeto relacionado se toman solo dos campos.
    @Mapping(source = "duenio.id", target = "duenioId")
    @Mapping(source = "duenio.nombre", target = "duenioNombre")
    MascotaDTO toDTO(Mascota mascota);

    List<MascotaDTO> toDTOList(List<Mascota> mascotas);
}
