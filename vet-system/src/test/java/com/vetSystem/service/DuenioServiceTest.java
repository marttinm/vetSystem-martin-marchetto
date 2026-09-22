package com.vetSystem.service;

import com.vetSystem.Dto.DuenioDTO;
import com.vetSystem.Entity.Duenio;
import com.vetSystem.Exception.DuplicateResourceException;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Mapper.DuenioMapper;
import com.vetSystem.Repository.DuenioRepository;
import com.vetSystem.Service.DuenioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DuenioServiceTest {
    @InjectMocks
    private DuenioService duenioService;
    @Mock
    private DuenioMapper duenioMapper;
    @Mock
    private DuenioRepository duenioRepository;

    //Test 1: Obtener todos los duenios cuando no hay ninguno
    @Test
    public void getAllDuenios_cuandoListaVacia_retornaListaVacia() {
        //DADO
        when(duenioRepository.findAll()).thenReturn(List.of());
        when(duenioMapper.toDTOList(List.of())).thenReturn(List.of());
        //CUANDO
        List<DuenioDTO> resultado = duenioService.getAllDuenios();
        //ENTONCES
        assertThat(resultado).isEmpty();
        verify(duenioRepository).findAll();
    }

    //Test 2: Obtener todos los duenios cuando hay datos
    @Test
    public void getAllDuenios_cuandoHayDuenios_retornaLista() {
        //DADO
        Duenio duenio = crearDuenio();
        DuenioDTO duenioDTO = crearDuenioDTO();
        when(duenioRepository.findAll()).thenReturn(List.of(duenio));
        when(duenioMapper.toDTOList(List.of(duenio))).thenReturn(List.of(duenioDTO));
        //CUANDO
        List<DuenioDTO> resultado = duenioService.getAllDuenios();
        //ENTONCES
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Carlos");
        verify(duenioRepository).findAll();
    }

    //Test 3: Buscar duenio por id cuando existe
    @Test
    public void getDuenioById_cuandoExiste_retornaDTO() {
        //DADO
        Duenio duenio = crearDuenio();
        DuenioDTO duenioDTO = crearDuenioDTO();
        when(duenioRepository.findById(1L)).thenReturn(Optional.of(duenio));
        when(duenioMapper.toDTO(duenio)).thenReturn(duenioDTO);
        //CUANDO
        DuenioDTO resultado = duenioService.getDuenioById(1L);
        //ENTONCES
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Carlos");
        verify(duenioRepository).findById(1L);
    }

    //Test 4: Buscar duenio por id cuando no existe
    @Test
    public void getDuenioById_cuandoNoExiste_lanzaResourceNotFoundException() {
        //DADO
        when(duenioRepository.findById(99L)).thenReturn(Optional.empty());
        //CUANDO
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> duenioService.getDuenioById(99L));
        //ENTONCES
        assertThat(ex.getMessage()).isEqualTo("Duenio con id 99 no fue encontrado");
        verify(duenioRepository).findById(99L);
        verify(duenioMapper, never()).toDTO(any());
    }

    //Test 5: Crear duenio con datos validos
    @Test
    public void createDuenio_cuandoDniNoExiste_guardaYRetornaDTO() {
        //DADO
        DuenioDTO entrada = crearDuenioDTO();
        Duenio duenio = crearDuenio();
        when(duenioRepository.existsByDni("31541741")).thenReturn(false);
        when(duenioMapper.toEntity(entrada)).thenReturn(duenio);
        when(duenioRepository.save(duenio)).thenReturn(duenio);
        when(duenioMapper.toDTO(duenio)).thenReturn(entrada);
        //CUANDO
        DuenioDTO resultado = duenioService.createDuenio(entrada);
        //ENTONCES
        assertThat(resultado.getNombre()).isEqualTo("Carlos");
        verify(duenioRepository).save(duenio);
    }

    //Test 6: Crear duenio con un DNI que ya existe
    @Test
    public void createDuenio_cuandoDniDuplicado_lanzaDuplicateResourceException() {
        //DADO
        DuenioDTO entrada = crearDuenioDTO();
        when(duenioRepository.existsByDni("31541741")).thenReturn(true);
        //CUANDO
        assertThrows(DuplicateResourceException.class,
                () -> duenioService.createDuenio(entrada));
        //ENTONCES
        verify(duenioRepository, never()).save(any());
    }

    private Duenio crearDuenio() {
        Duenio duenio = new Duenio();
        duenio.setId(1L);
        duenio.setNombre("Carlos");
        duenio.setApellido("Sanchez");
        duenio.setDni("31541741");
        duenio.setTelefono(1230222);
        duenio.setEmail("carlossanchez@gmail.com");
        return duenio;
    }

    private DuenioDTO crearDuenioDTO() {
        DuenioDTO duenioDTO = new DuenioDTO();
        duenioDTO.setId(1L);
        duenioDTO.setNombre("Carlos");
        duenioDTO.setApellido("Sanchez");
        duenioDTO.setDni("31541741");
        duenioDTO.setTelefono(1230222);
        duenioDTO.setEmail("carlossanchez@gmail.com");
        return duenioDTO;
    }
}
