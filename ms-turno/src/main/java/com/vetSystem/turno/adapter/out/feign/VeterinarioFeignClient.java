package com.vetSystem.turno.adapter.out.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// El name tiene que coincidir con el spring.application.name de ms-veterinario registrado en Eureka
@FeignClient(name = "ms-veterinario")
public interface VeterinarioFeignClient {

    @GetMapping(value = "/api/veterinarios/{id}", produces = "application/json")
    VeterinarioResponse findById(@PathVariable("id") Long id);
}
