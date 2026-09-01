package com.vetSystem.Mapper;

import com.vetSystem.Dto.VeterinarioDTO;
import com.vetSystem.Entity.Veterinario;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VeterinarioMapper {
    VeterinarioDTO toDTO(Veterinario veterinario);
    Veterinario toEntity(VeterinarioDTO dto);
    List<VeterinarioDTO> toDTOList(List<Veterinario> veterinarios);
}
