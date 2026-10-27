package com.vetSystem.veterinario.adapter.in.web.dto;

import com.vetSystem.veterinario.domain.model.Veterinario;

public final class VeterinarioMapper {

    private VeterinarioMapper() {
    }

    public static Veterinario toEntity(VeterinarioRequest request) {
        Veterinario veterinario = new Veterinario();
        veterinario.setNombre(request.getNombre());
        veterinario.setApellido(request.getApellido());
        veterinario.setMatricula(request.getMatricula());
        veterinario.setEspecialidad(request.getEspecialidad());
        return veterinario;
    }

    public static VeterinarioResponse toResponse(Veterinario veterinario) {
        return new VeterinarioResponse(
                veterinario.getId(),
                veterinario.getNombre(),
                veterinario.getApellido(),
                veterinario.getMatricula(),
                veterinario.getEspecialidad());
    }
}
