package com.vetSystem.Controller;

import com.vetSystem.Dto.TurnoRequestDTO;
import com.vetSystem.Dto.TurnoResponseDTO;
import com.vetSystem.Entity.EstadoTurno;
import com.vetSystem.Exception.ErrorResponse;
import com.vetSystem.Service.TurnoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Turnos", description = "Gestion de turnos: alta, agenda de veterinarios y cambios de estado")
@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    @Operation(
            summary = "Listar todos los turnos",
            description = "Devuelve todos los turnos registrados. Si no hay ninguno devuelve una lista vacia")
    @ApiResponse(responseCode = "200", description = "Lista de turnos")
    @GetMapping
    public ResponseEntity<List<TurnoResponseDTO>> getAllTurnos() {
        return ResponseEntity.ok(turnoService.getAllTurnos());
    }

    @Operation(
            summary = "Buscar turno por ID",
            description = "Devuelve el turno con el ID indicado. Si no existe devuelve 404")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turno encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un turno con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TurnoResponseDTO> getTurnoById(
            @Parameter(description = "ID del turno a buscar", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(turnoService.getTurnoById(id));
    }

    @Operation(
            summary = "Agenda de un veterinario",
            description = "Devuelve los turnos de un veterinario en una fecha. Si el veterinario no existe devuelve 404")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turnos del veterinario en esa fecha"),
            @ApiResponse(responseCode = "404", description = "No existe un veterinario con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/agenda")
    public ResponseEntity<?> getAgenda(
            @Parameter(description = "ID del veterinario", example = "1") @RequestParam Long veterinarioId,
            @Parameter(description = "Fecha de la agenda (yyyy-MM-dd)", example = "2026-10-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(turnoService.getAgenda(veterinarioId, fecha));
    }

    @Operation(
            summary = "Registrar un nuevo turno",
            description = "Crea un turno en estado PENDIENTE. La fecha no puede ser pasada. Falla si la mascota o el veterinario no existen, o si el veterinario ya tiene un turno en esa fecha y hora")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Turno creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Los datos son invalidos (falla de validacion)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe la mascota o el veterinario",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El veterinario ya tiene un turno en ese horario",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<?> createTurno(@Valid @RequestBody TurnoRequestDTO request) {
        TurnoResponseDTO nuevo = turnoService.createTurno(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @Operation(
            summary = "Cambiar el estado de un turno",
            description = "Actualiza el estado del turno y, opcionalmente, sus observaciones. Si el turno no existe devuelve 404")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "404", description = "No existe un turno con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<TurnoResponseDTO> actualizarEstado(
            @Parameter(description = "ID del turno", example = "1") @PathVariable Long id,
            @Parameter(description = "Nuevo estado del turno", example = "FINALIZADO") @RequestParam EstadoTurno estado,
            @Parameter(description = "Observaciones del veterinario", example = "Vacuna antirrabica aplicada")
            @RequestParam(required = false) String observaciones) {
        return ResponseEntity.ok(turnoService.actualizarEstado(id, estado, observaciones));
    }

    @Operation(
            summary = "Eliminar un turno",
            description = "Elimina el turno con el ID indicado. Si no existe devuelve 404")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Turno eliminado"),
            @ApiResponse(responseCode = "404", description = "No existe un turno con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTurno(
            @Parameter(description = "ID del turno a eliminar", example = "1") @PathVariable Long id) {
        turnoService.deleteTurno(id);
        return ResponseEntity.noContent().build();
    }
}
