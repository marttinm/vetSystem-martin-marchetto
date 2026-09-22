package com.vetSystem.Controller;

import com.vetSystem.Dto.DuenioDTO;
import com.vetSystem.Dto.MascotaDTO;
import com.vetSystem.Exception.ErrorResponse;
import com.vetSystem.Service.DuenioService;
import com.vetSystem.Service.MascotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Duenios", description = "CRUD de los duenios de la clinica veterinaria")
@RestController
@RequestMapping("/api/duenios")
@RequiredArgsConstructor
public class DuenioController {

    private final DuenioService duenioService;
    private final MascotaService mascotaService;

    @Operation(
            summary = "Buscar duenio por ID",
            description = "Devuelve los datos del duenio con el ID indicado. Si no existe devuelve 404")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Duenio encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un duenio con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<DuenioDTO> getDuenioById(
            @Parameter(description = "ID del duenio a buscar", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(duenioService.getDuenioById(id));
    }

    @Operation(
            summary = "Listar todos los duenios",
            description = "Devuelve la lista completa de duenios registrados. Si no hay ninguno devuelve una lista vacia")
    @ApiResponse(responseCode = "200", description = "Lista de duenios")
    @GetMapping
    public ResponseEntity<List<DuenioDTO>> getAllDuenios() {
        return ResponseEntity.ok(duenioService.getAllDuenios());
    }

    @Operation(
            summary = "Registrar un nuevo duenio",
            description = "Crea un nuevo duenio. Nombre, apellido, DNI (7 u 8 digitos) y email son obligatorios. Falla si el DNI ya esta registrado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Duenio creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Los datos son invalidos (falla de validacion)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe un duenio con ese DNI",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<?> createDuenio(@Valid @RequestBody DuenioDTO duenio) {
        DuenioDTO nuevoDuenio = duenioService.createDuenio(duenio);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoDuenio);
    }

    @Operation(
            summary = "Actualizar un duenio",
            description = "Actualiza nombre, apellido, telefono y email del duenio. El DNI no se modifica. Falla si el duenio no existe o los datos son invalidos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Duenio actualizado"),
            @ApiResponse(responseCode = "400", description = "Los datos son invalidos (falla de validacion)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un duenio con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<DuenioDTO> updateDuenio(
            @Parameter(description = "ID del duenio a actualizar", example = "1") @PathVariable Long id,
            @Valid @RequestBody DuenioDTO duenio) {
        return ResponseEntity.ok(duenioService.updateDuenio(id, duenio));
    }

    @Operation(
            summary = "Eliminar un duenio",
            description = "Elimina el duenio con el ID indicado. Si no existe devuelve 404")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Duenio eliminado"),
            @ApiResponse(responseCode = "404", description = "No existe un duenio con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDuenio(
            @Parameter(description = "ID del duenio a eliminar", example = "1") @PathVariable Long id) {
        duenioService.deleteDuenio(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Listar las mascotas de un duenio",
            description = "Devuelve todas las mascotas del duenio indicado. Si el duenio no existe devuelve 404")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de mascotas del duenio"),
            @ApiResponse(responseCode = "404", description = "No existe un duenio con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/mascotas")
    public ResponseEntity<List<MascotaDTO>> getMascotasByDueno(
            @Parameter(description = "ID del duenio", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(mascotaService.getMascotasByDueno(id));
    }
}
