package com.vetSystem.Mapper;

import com.vetSystem.Dto.DuenioDTO;
import com.vetSystem.Entity.Duenio;
import org.mapstruct.Mapper;

import java.util.List;

/** componentModel = "spring" hace que MapStruct genere la implementacion
 *  como un @Component, para poder inyectarla por constructor. */
@Mapper(componentModel = "spring")
public interface DuenioMapper {
    DuenioDTO toDTO(Duenio duenio);
    Duenio toEntity(DuenioDTO dto);
    List<DuenioDTO> toDTOList(List<Duenio> duenios);
}
