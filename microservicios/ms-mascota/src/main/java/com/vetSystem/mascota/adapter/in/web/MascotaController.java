package com.vetSystem.mascota.adapter.in.web;

import com.vetSystem.mascota.adapter.in.web.dto.MascotaMapper;
import com.vetSystem.mascota.adapter.in.web.dto.MascotaRequest;
import com.vetSystem.mascota.adapter.in.web.dto.MascotaResponse;
import com.vetSystem.mascota.domain.port.in.MascotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
@RequiredArgsConstructor
public class MascotaController {

    private final MascotaService mascotaService;

    @GetMapping
    public ResponseEntity<List<MascotaResponse>> findAll() {
        return ResponseEntity.ok(mascotaService.findAll().stream()
                .map(MascotaMapper::toResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MascotaResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(MascotaMapper.toResponse(mascotaService.findById(id)));
    }

    @GetMapping("/duenio/{duenioId}")
    public ResponseEntity<List<MascotaResponse>> findByDuenioId(@PathVariable Long duenioId) {
        return ResponseEntity.ok(mascotaService.findByDuenioId(duenioId).stream()
                .map(MascotaMapper::toResponse)
                .toList());
    }

    @PostMapping
    public ResponseEntity<MascotaResponse> save(@Valid @RequestBody MascotaRequest request) {
        MascotaResponse creada = MascotaMapper.toResponse(mascotaService.save(MascotaMapper.toEntity(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MascotaResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody MascotaRequest request) {
        return ResponseEntity.ok(MascotaMapper.toResponse(mascotaService.update(id, MascotaMapper.toEntity(request))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        mascotaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
