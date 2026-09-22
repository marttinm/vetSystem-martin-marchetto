package com.vetSystem.controller;

import com.vetSystem.Controller.DuenioController;
import com.vetSystem.Dto.DuenioDTO;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Service.DuenioService;
import com.vetSystem.Service.MascotaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DuenioController.class)
public class DuenioControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private DuenioService duenioService;
    @MockitoBean
    private MascotaService mascotaService;

    //Test 1: GET /api/duenios cuando no hay duenios
    @Test
    public void getAllDuenios_cuandoListaVacia_retorna200() throws Exception {
        //DADO
        when(duenioService.getAllDuenios()).thenReturn(List.of());
        //CUANDO
        mockMvc.perform(get("/api/duenios"))
        //ENTONCES
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    //Test 2: GET /api/duenios/1 cuando el duenio existe
    @Test
    public void getDuenioById_cuandoExiste_retorna200() throws Exception {
        //DADO
        DuenioDTO duenioDTO = new DuenioDTO(1L, "Carlos", "Sanchez", "31541741", 1230222, "carlossanchez@gmail.com");
        when(duenioService.getDuenioById(1L)).thenReturn(duenioDTO);
        //CUANDO
        mockMvc.perform(get("/api/duenios/1"))
        //ENTONCES
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Carlos"));
    }

    //Test 3: GET /api/duenios/99 cuando el duenio no existe
    @Test
    public void getDuenioById_cuandoNoExiste_retorna404() throws Exception {
        //DADO
        when(duenioService.getDuenioById(99L)).thenThrow(new ResourceNotFoundException("Duenio", 99L));
        //CUANDO
        mockMvc.perform(get("/api/duenios/99"))
        //ENTONCES
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Duenio con id 99 no fue encontrado"));
    }

    //Test 4: POST /api/duenios con body valido
    @Test
    public void createDuenio_cuandoBodyValido_retorna201() throws Exception {
        //DADO
        String body = """
                {"nombre":"Carlos","apellido":"Sanchez","dni":"31541741","telefono":1230222,"email":"carlossanchez@gmail.com"}
                """;
        DuenioDTO creado = new DuenioDTO(1L, "Carlos", "Sanchez", "31541741", 1230222, "carlossanchez@gmail.com");
        when(duenioService.createDuenio(any(DuenioDTO.class))).thenReturn(creado);
        //CUANDO
        mockMvc.perform(post("/api/duenios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
        //ENTONCES
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Carlos"));
    }

    //Test 5: POST /api/duenios con email vacio
    @Test
    public void createDuenio_cuandoEmailVacio_retorna400() throws Exception {
        //DADO
        String body = """
                {"nombre":"Carlos","apellido":"Sanchez","dni":"31541741","telefono":1230222,"email":""}
                """;
        //CUANDO
        mockMvc.perform(post("/api/duenios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
        //ENTONCES
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verify(duenioService, never()).createDuenio(any());
    }
}
