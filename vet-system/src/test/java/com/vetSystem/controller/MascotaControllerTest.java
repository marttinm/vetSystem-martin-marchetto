package com.vetSystem.controller;

import com.vetSystem.Controller.MascotaController;
import com.vetSystem.Service.MascotaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MascotaController.class)
public class MascotaControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private MascotaService mascotaService;

    //Test 1: POST /api/mascotas sin el parametro obligatorio duenioId
    @Test
    public void createMascota_cuandoFaltaDuenioId_retorna400() throws Exception {
        //DADO
        String body = """
                {"nombre":"Toby","especie":"Perro","raza":"Mestizo","fechaNacimiento":"2020-01-01"}
                """;
        //CUANDO
        mockMvc.perform(post("/api/mascotas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
        //ENTONCES
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").value("Falta el parametro obligatorio duenioId"));
        verify(mascotaService, never()).createMascota(any(), any());
    }
}
