package com.vetSystem.Controller;

import com.vetSystem.Dto.MascotaDTO;
import jakarta.validation.Valid;
import com.vetSystem.Entity.Mascota;
import com.vetSystem.Service.MascotaService;
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

    @GetMapping("/{id}")
    public ResponseEntity<MascotaDTO> getMascotaById(@PathVariable Long id) {
        return ResponseEntity.ok(mascotaService.getMascotaById(id));
    }

    @GetMapping
    public ResponseEntity<List<MascotaDTO>> getAllMascotas(){
        return ResponseEntity.ok(mascotaService.getAllMascotas());
    }

    @PostMapping
    public ResponseEntity<?> createMascota(@RequestParam Long duenioId, @Valid @RequestBody MascotaDTO mascota){
        MascotaDTO nuevaMascota = mascotaService.createMascota(duenioId, mascota);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaMascota);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MascotaDTO> updateMascota(@PathVariable Long id, @Valid @RequestBody MascotaDTO mascota){
        MascotaDTO updatedMascota = mascotaService.updateMascota(id, mascota);
        return ResponseEntity.ok(updatedMascota);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMascota(@PathVariable Long id) {
        mascotaService.deleteMascota(id);
        return ResponseEntity.noContent().build();
    }

}

