package com.vetSystem.service;

import com.vetSystem.Dto.TurnoRequestDTO;
import com.vetSystem.Dto.TurnoResponseDTO;
import com.vetSystem.Entity.EstadoTurno;
import com.vetSystem.Entity.Mascota;
import com.vetSystem.Entity.Turno;
import com.vetSystem.Entity.Veterinario;
import com.vetSystem.Exception.TurnoSuperpuestoException;
import com.vetSystem.Mapper.MedicamentoMapper;
import com.vetSystem.Mapper.TurnoMapper;
import com.vetSystem.Repository.MascotaRepository;
import com.vetSystem.Repository.MedicamentoRepository;
import com.vetSystem.Repository.TurnoRepository;
import com.vetSystem.Repository.VeterinarioRepository;
import com.vetSystem.Service.TurnoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TurnoServiceTest {
    @InjectMocks
    private TurnoService turnoService;
    @Mock
    private TurnoRepository turnoRepository;
    @Mock
    private MascotaRepository mascotaRepository;
    @Mock
    private VeterinarioRepository veterinarioRepository;
    @Mock
    private MedicamentoRepository medicamentoRepository;
    @Mock
    private TurnoMapper turnoMapper;
    @Mock
    private MedicamentoMapper medicamentoMapper;

    private static final LocalDate FECHA = LocalDate.now().plusDays(1);
    private static final LocalTime HORA = LocalTime.of(10, 0);

    //Test 1: Crear turno cuando la mascota y el veterinario existen y el horario esta libre
    @Test
    public void createTurno_cuandoHorarioLibre_guardaTurnoPendiente() {
        //DADO
        TurnoRequestDTO request = crearRequest();
        Mascota mascota = new Mascota();
        mascota.setId(1L);
        Veterinario veterinario = new Veterinario();
        veterinario.setId(2L);
        TurnoResponseDTO respuesta = new TurnoResponseDTO();
        respuesta.setId(10L);
        respuesta.setEstado(EstadoTurno.PENDIENTE);

        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(mascota));
        when(veterinarioRepository.findById(2L)).thenReturn(Optional.of(veterinario));
        when(turnoRepository.findFirstByVeterinarioIdAndFechaAndHora(2L, FECHA, HORA)).thenReturn(Optional.empty());
        when(turnoRepository.save(any(Turno.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(turnoMapper.toDTO(any(Turno.class))).thenReturn(respuesta);
        //CUANDO
        TurnoResponseDTO resultado = turnoService.createTurno(request);
        //ENTONCES
        assertThat(resultado.getId()).isEqualTo(10L);
        assertThat(resultado.getEstado()).isEqualTo(EstadoTurno.PENDIENTE);
        verify(turnoRepository, times(1)).save(any(Turno.class));
    }

    //Test 2: Crear turno cuando el veterinario ya tiene un turno en ese horario
    @Test
    public void createTurno_cuandoHaySuperposicion_lanzaTurnoSuperpuestoException() {
        //DADO
        TurnoRequestDTO request = crearRequest();
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(new Mascota()));
        when(veterinarioRepository.findById(2L)).thenReturn(Optional.of(new Veterinario()));
        Turno existente = new Turno();
        existente.setId(7L);
        existente.setFecha(FECHA);
        existente.setHora(HORA);
        when(turnoRepository.findFirstByVeterinarioIdAndFechaAndHora(2L, FECHA, HORA)).thenReturn(Optional.of(existente));
        //CUANDO
        TurnoSuperpuestoException ex = assertThrows(TurnoSuperpuestoException.class,
                () -> turnoService.createTurno(request));
        //ENTONCES
        assertThat(ex.getMessage()).contains("id 7").contains(HORA.toString());
        verify(turnoRepository, never()).save(any());
    }

    private TurnoRequestDTO crearRequest() {
        TurnoRequestDTO request = new TurnoRequestDTO();
        request.setFecha(FECHA);
        request.setHora(HORA);
        request.setMotivo("Control anual");
        request.setMascotaId(1L);
        request.setVeterinarioId(2L);
        return request;
    }
}
