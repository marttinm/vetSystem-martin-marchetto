package com.vetSystem.turno.adapter.in.web;

import com.vetSystem.turno.adapter.in.web.dto.TurnoMapper;
import com.vetSystem.turno.adapter.in.web.dto.TurnoRequest;
import com.vetSystem.turno.adapter.in.web.dto.TurnoResponse;
import com.vetSystem.turno.domain.port.in.TurnoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    @GetMapping
    public ResponseEntity<List<TurnoResponse>> findAll() {
        return ResponseEntity.ok(turnoService.findAll().stream()
                .map(TurnoMapper::toResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurnoResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(TurnoMapper.toResponse(turnoService.findById(id)));
    }

    @GetMapping("/veterinario/{veterinarioId}")
    public ResponseEntity<List<TurnoResponse>> findByVeterinarioId(@PathVariable Long veterinarioId) {
        return ResponseEntity.ok(turnoService.findByVeterinarioId(veterinarioId).stream()
                .map(TurnoMapper::toResponse)
                .toList());
    }

    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<List<TurnoResponse>> findByMascotaId(@PathVariable Long mascotaId) {
        return ResponseEntity.ok(turnoService.findByMascotaId(mascotaId).stream()
                .map(TurnoMapper::toResponse)
                .toList());
    }

    @PostMapping
    public ResponseEntity<TurnoResponse> save(@Valid @RequestBody TurnoRequest request) {
        TurnoResponse creado = TurnoMapper.toResponse(turnoService.save(TurnoMapper.toEntity(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<TurnoResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(TurnoMapper.toResponse(turnoService.cancelar(id)));
    }
}
