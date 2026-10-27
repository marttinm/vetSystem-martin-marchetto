package com.vetSystem.duenio.adapter.in.web;

import com.vetSystem.duenio.adapter.in.web.dto.DuenioMapper;
import com.vetSystem.duenio.adapter.in.web.dto.DuenioRequest;
import com.vetSystem.duenio.adapter.in.web.dto.DuenioResponse;
import com.vetSystem.duenio.domain.port.in.DuenioService;
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

    @GetMapping
    public ResponseEntity<List<DuenioResponse>> findAll() {
        return ResponseEntity.ok(duenioService.findAll().stream()
                .map(DuenioMapper::toResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DuenioResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(DuenioMapper.toResponse(duenioService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<DuenioResponse> save(@Valid @RequestBody DuenioRequest request) {
        DuenioResponse creado = DuenioMapper.toResponse(duenioService.save(DuenioMapper.toEntity(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DuenioResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody DuenioRequest request) {
        return ResponseEntity.ok(DuenioMapper.toResponse(duenioService.update(id, DuenioMapper.toEntity(request))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        duenioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
