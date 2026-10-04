package com.vetSystem.Mapper;

import com.vetSystem.Dto.MedicamentoRequestDTO;
import com.vetSystem.Dto.MedicamentoResponseDTO;
import com.vetSystem.Entity.Medicamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MedicamentoMapper {
    MedicamentoResponseDTO toDTO(Medicamento medicamento);

    @Mapping(target = "id", ignore = true)
    Medicamento toEntity(MedicamentoRequestDTO dto);

    List<MedicamentoResponseDTO> toDTOList(List<Medicamento> medicamentos);
}
