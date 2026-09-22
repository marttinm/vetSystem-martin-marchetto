package com.vetSystem.Controller;

import com.vetSystem.Dto.VeterinarioDTO;
import com.vetSystem.Service.VeterinarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Veterinarios", description = "CRUD de los veterinarios de la clinica")
@RestController
@RequestMapping("/api/veterinarios")
@RequiredArgsConstructor
public class VeterinarioController {

    private final VeterinarioService veterinarioService;

    @GetMapping
    public ResponseEntity<List<VeterinarioDTO>> getAllVeterinarios() {
        return ResponseEntity.ok(veterinarioService.getAllVeterinarios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeterinarioDTO> getVeterinarioById(@PathVariable Long id) {
        return ResponseEntity.ok(veterinarioService.getVeterinarioById(id));
    }

    @PostMapping
    public ResponseEntity<?> createVeterinario(@Valid @RequestBody VeterinarioDTO dto) {
        VeterinarioDTO nuevo = veterinarioService.createVeterinario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeterinarioDTO> updateVeterinario(@PathVariable Long id,
                                                            @Valid @RequestBody VeterinarioDTO dto) {
        return ResponseEntity.ok(veterinarioService.updateVeterinario(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVeterinario(@PathVariable Long id) {
        veterinarioService.deleteVeterinario(id);
        return ResponseEntity.noContent().build();
    }
}
