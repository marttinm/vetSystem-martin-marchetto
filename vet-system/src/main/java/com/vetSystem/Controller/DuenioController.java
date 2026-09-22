package com.vetSystem.Controller;

import com.vetSystem.Dto.DuenioDTO;
import com.vetSystem.Dto.MascotaDTO;
import com.vetSystem.Service.DuenioService;
import com.vetSystem.Service.MascotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/duenios")
@RequiredArgsConstructor
public class DuenioController {

    private final DuenioService duenioService;
    private final MascotaService mascotaService;

    @GetMapping("/{id}")
    public ResponseEntity<DuenioDTO> getDuenioById(@PathVariable Long id) {
        return ResponseEntity.ok(duenioService.getDuenioById(id));
    }

    @GetMapping
    public ResponseEntity<List<DuenioDTO>> getAllDuenios() {
        return ResponseEntity.ok(duenioService.getAllDuenios());
    }

    @PostMapping
    public ResponseEntity<?> createDuenio(@Valid @RequestBody DuenioDTO duenio) {
        DuenioDTO nuevoDuenio = duenioService.createDuenio(duenio);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoDuenio);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DuenioDTO> updateDuenio(@PathVariable Long id, @Valid @RequestBody DuenioDTO duenio) {
        return ResponseEntity.ok(duenioService.updateDuenio(id, duenio));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDuenio(@PathVariable Long id) {
        duenioService.deleteDuenio(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/mascotas")
    public ResponseEntity<List<MascotaDTO>> getMascotasByDueno(@PathVariable Long id) {
        return ResponseEntity.ok(mascotaService.getMascotasByDueno(id));
    }
}
