package com.vetSystem.Controller;

import com.vetSystem.Entity.Mascota;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Service.MascotaService;
import jakarta.persistence.EntityExistsException;
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
    public ResponseEntity<Mascota> getMascotaById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(mascotaService.getMascotaById(id));
        } catch ( ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Mascota>> getAllMascotas(){
        return ResponseEntity.ok(mascotaService.getAllMascotas());
    }

    @PostMapping
    public ResponseEntity<?> createMascota(@RequestParam Long duenioId, @RequestBody Mascota mascota){
        try {
            Mascota nuevaMascota = mascotaService.createMascota(duenioId, mascota);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaMascota);
        } catch ( ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch ( EntityExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mascota> updateMascota(@PathVariable Long id, @RequestBody Mascota mascota){
        try {
            Mascota updatedMascota = mascotaService.updateMascota(id, mascota);
            return ResponseEntity.ok(updatedMascota);
        } catch ( ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMascota(@PathVariable Long id) {
        try {
            mascotaService.deleteMascota(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

}

