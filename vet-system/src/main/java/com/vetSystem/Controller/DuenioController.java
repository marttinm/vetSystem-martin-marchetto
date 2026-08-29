package com.vetSystem.Controller;

import com.vetSystem.Entity.Duenio;
import com.vetSystem.Entity.Mascota;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Service.DuenioService;
import com.vetSystem.Service.MascotaService;
import jakarta.persistence.EntityExistsException;
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
    public ResponseEntity<Duenio> getDuenioById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(duenioService.getDuenioById(id));
        } catch ( ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Duenio>> getAllDuenios(){
        return ResponseEntity.ok(duenioService.getAllDuenios());
    }

    @PostMapping
    public ResponseEntity<?> createDuenio(@RequestBody Duenio duenio){
        try {
            Duenio nuevoDuenio = duenioService.createDuenio(duenio);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoDuenio);
        } catch ( EntityExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Duenio> updateDuenio(@PathVariable Long id, @RequestBody Duenio duenio){
        try {
            Duenio updatedDuenio = duenioService.updateDuenio(id, duenio);
            return ResponseEntity.ok(updatedDuenio);
        } catch ( ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDuenio(@PathVariable Long id) {
        try {
            duenioService.deleteDuenio(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/mascotas")
    public ResponseEntity<List<Mascota>> getMascotasByDueno(@PathVariable Long id){
        try {
            return ResponseEntity.ok(mascotaService.getMascotasByDueno(id));
        } catch (ResourceNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }
}
