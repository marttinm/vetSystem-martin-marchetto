package com.vetSystem.turno.adapter.out.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// El name tiene que coincidir con el spring.application.name de ms-mascota registrado en Eureka
@FeignClient(name = "ms-mascota")
public interface MascotaFeignClient {

    @GetMapping(value = "/api/mascotas/{id}", produces = "application/json")
    MascotaResponse findById(@PathVariable("id") Long id);
}
