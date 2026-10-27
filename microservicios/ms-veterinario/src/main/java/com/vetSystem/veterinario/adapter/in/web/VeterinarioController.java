package com.vetSystem.veterinario.adapter.in.web;

import com.vetSystem.veterinario.adapter.in.web.dto.VeterinarioMapper;
import com.vetSystem.veterinario.adapter.in.web.dto.VeterinarioRequest;
import com.vetSystem.veterinario.adapter.in.web.dto.VeterinarioResponse;
import com.vetSystem.veterinario.domain.port.in.VeterinarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veterinarios")
@RequiredArgsConstructor
public class VeterinarioController {

    private final VeterinarioService veterinarioService;

    @GetMapping
    public ResponseEntity<List<VeterinarioResponse>> findAll() {
        return ResponseEntity.ok(veterinarioService.findAll().stream()
                .map(VeterinarioMapper::toResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeterinarioResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(VeterinarioMapper.toResponse(veterinarioService.findById(id)));
    }

    @GetMapping("/especialidad/{especialidad}")
    public ResponseEntity<List<VeterinarioResponse>> findByEspecialidad(@PathVariable String especialidad) {
        return ResponseEntity.ok(veterinarioService.findByEspecialidad(especialidad).stream()
                .map(VeterinarioMapper::toResponse)
                .toList());
    }

    @PostMapping
    public ResponseEntity<VeterinarioResponse> save(@Valid @RequestBody VeterinarioRequest request) {
        VeterinarioResponse creado = VeterinarioMapper.toResponse(
                veterinarioService.save(VeterinarioMapper.toEntity(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeterinarioResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody VeterinarioRequest request) {
        return ResponseEntity.ok(VeterinarioMapper.toResponse(
                veterinarioService.update(id, VeterinarioMapper.toEntity(request))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        veterinarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
