package com.vetSystem.Controller;

import com.vetSystem.Dto.TurnoRequestDTO;
import com.vetSystem.Dto.TurnoResponseDTO;
import com.vetSystem.Entity.EstadoTurno;
import com.vetSystem.Service.TurnoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    @GetMapping
    public ResponseEntity<List<TurnoResponseDTO>> getAllTurnos() {
        return ResponseEntity.ok(turnoService.getAllTurnos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurnoResponseDTO> getTurnoById(@PathVariable Long id) {
        return ResponseEntity.ok(turnoService.getTurnoById(id));
    }

    @GetMapping("/agenda")
    public ResponseEntity<?> getAgenda(@RequestParam Long veterinarioId,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(turnoService.getAgenda(veterinarioId, fecha));
    }

    @PostMapping
    public ResponseEntity<?> createTurno(@Valid @RequestBody TurnoRequestDTO request) {
        TurnoResponseDTO nuevo = turnoService.createTurno(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<TurnoResponseDTO> actualizarEstado(
            @PathVariable Long id,
            @RequestParam EstadoTurno estado,
            @RequestParam(required = false) String observaciones) {
        return ResponseEntity.ok(turnoService.actualizarEstado(id, estado, observaciones));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTurno(@PathVariable Long id) {
        turnoService.deleteTurno(id);
        return ResponseEntity.noContent().build();
    }
}
