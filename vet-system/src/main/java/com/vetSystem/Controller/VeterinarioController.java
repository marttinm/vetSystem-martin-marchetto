package com.vetSystem.Controller;

import com.vetSystem.Dto.VeterinarioDTO;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Service.VeterinarioService;
import jakarta.persistence.EntityExistsException;
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
    public ResponseEntity<List<VeterinarioDTO>> getAllVeterinarios() {
        return ResponseEntity.ok(veterinarioService.getAllVeterinarios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeterinarioDTO> getVeterinarioById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(veterinarioService.getVeterinarioById(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createVeterinario(@RequestBody VeterinarioDTO dto) {
        try {
            VeterinarioDTO nuevo = veterinarioService.createVeterinario(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (EntityExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeterinarioDTO> updateVeterinario(@PathVariable Long id,
                                                            @RequestBody VeterinarioDTO dto) {
        try {
            return ResponseEntity.ok(veterinarioService.updateVeterinario(id, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVeterinario(@PathVariable Long id) {
        try {
            veterinarioService.deleteVeterinario(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
