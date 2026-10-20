package com.vetSystem.config;

import com.vetSystem.Controller.DuenioController;
import com.vetSystem.Service.DuenioService;
import com.vetSystem.Service.MascotaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(DuenioController.class)
@ExtendWith(OutputCaptureExtension.class)
public class RequestLoggingFilterTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private DuenioService duenioService;
    @MockitoBean
    private MascotaService mascotaService;

    //Test 1: un request exitoso deja una linea de log en JSON con path y status_code
    @Test
    public void requestExitoso_logueaJsonConPathYStatus(CapturedOutput output) throws Exception {
        //DADO
        when(duenioService.getAllDuenios()).thenReturn(List.of());
        //CUANDO
        mockMvc.perform(get("/api/duenios"));
        //ENTONCES
        String linea = lineaDelRequest(output, "/api/duenios");
        assertThat(linea).contains("\"timestamp\":");
        assertThat(linea).contains("\"level\":\"INFO\"");
        assertThat(linea).contains("\"method\":\"GET\"");
        assertThat(linea).contains("\"status_code\":200");
        assertThat(linea).contains("\"duration_ms\":");
    }

    //Test 2: un request a una ruta inexistente loguea el 404
    @Test
    public void rutaInexistente_logueaStatus404(CapturedOutput output) throws Exception {
        //CUANDO
        mockMvc.perform(get("/api/no-existe"));
        //ENTONCES
        String linea = lineaDelRequest(output, "/api/no-existe");
        assertThat(linea).contains("\"status_code\":404");
    }

    private String lineaDelRequest(CapturedOutput output, String path) {
        return output.getOut().lines()
                .filter(linea -> linea.startsWith("{") && linea.contains("\"path\":\"" + path + "\""))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No hay una linea de log JSON para " + path
                        + ". Salida:\n" + output.getOut()));
    }
}
