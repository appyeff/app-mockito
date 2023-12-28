package org.demo.appmockito.ejemplos.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.demo.appmockito.ejemplos.Datos;
import org.demo.appmockito.ejemplos.models.Examen;
import org.demo.appmockito.ejemplos.repositories.ExamenRepository;
import org.demo.appmockito.ejemplos.repositories.ExamenRepositoryImpl;
import org.demo.appmockito.ejemplos.repositories.PreguntaRepository;
import org.demo.appmockito.ejemplos.repositories.PreguntaRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TestExamenServiceImpl {

    @Mock
    ExamenRepositoryImpl examRepository;
    @Mock
    PreguntaRepositoryImpl questionRepository;

    @InjectMocks
    ExamenServiceImpl service;

    @Captor
    ArgumentCaptor<Long> capture;

    @BeforeEach
    void setUp() {
        //MockitoAnnotations.openMocks(this);
    }

    @Test
    void findExamenPorNombre() {

        when(examRepository.findAll()).thenReturn(Datos.EXAMENES);
        Optional<Examen> examen = service.findExamenPorNombre("Matematicas");

        assertTrue(examen.isPresent());
        assertEquals(5L, examen.orElseThrow().getId());
        assertEquals("Matematicas", examen.get().getNombre());
    }

    @Test
    void findExamenPorNombreListaVacia() {

        List<Examen> datos = Collections.emptyList();

        when(examRepository.findAll()).thenReturn(datos);
        Optional<Examen> examen = service.findExamenPorNombre("Matematicas");

        assertFalse(examen.isPresent());
    }

    @Test
    void testPreguntasExamen() {

        when(examRepository.findAll()).thenReturn(Datos.EXAMENES);
        when(questionRepository.findPreguntasPorExamenId(anyLong())).thenReturn(Datos.PREGUNTAS);

        Examen examen = service.findExamenPorNombreConPreguntas("Matematicas");
        assertEquals(5, examen.getPreguntas().size());
        assertTrue(examen.getPreguntas().contains("aritmética"));
    }

    @Test
    void testPreguntasExamenVerify() {

        when(examRepository.findAll()).thenReturn(Datos.EXAMENES);
        when(questionRepository.findPreguntasPorExamenId(anyLong())).thenReturn(Datos.PREGUNTAS);

        Examen examen = service.findExamenPorNombreConPreguntas("Matematicas");
        assertEquals(5, examen.getPreguntas().size());
        assertTrue(examen.getPreguntas().contains("integrales"));

        verify(examRepository).findAll();
        verify(questionRepository).findPreguntasPorExamenId(5L);
    }

    @Test
    void testNoExisteExamenVerify() {
        // given
        when(examRepository.findAll()).thenReturn(Datos.EXAMENES);
        when(questionRepository.findPreguntasPorExamenId(anyLong())).thenReturn(Datos.PREGUNTAS);

        // when
        Examen examen = service.findExamenPorNombreConPreguntas("Matematicas");
        assertNotNull(examen);
        // then
        verify(examRepository).findAll();
        verify(questionRepository).findPreguntasPorExamenId(5L);
    }

    @Test
    void testGuardarExamen() {
        Examen newExamen = Datos.EXAMEN;
        newExamen.setPreguntas(Datos.PREGUNTAS);

        when(examRepository.guardar(any(Examen.class))).then(new Answer<Examen>() {
            Long secuencia = 8L;

            @Override
            public Examen answer(InvocationOnMock invocationOnMock) throws Throwable {
                Examen examen = invocationOnMock.getArgument(0);
                examen.setId(secuencia++);
                return examen;
            }
        });

        // When
        Examen examen = service.guardar(newExamen);

        // Then
        assertNotNull(examen.getId());
        assertEquals(8L, examen.getId());
        assertNotEquals("Física", examen.getNombre());

        verify(examRepository).guardar(any(Examen.class));
        verify(questionRepository).guardarVarias(anyList());
    }

    @Test
    void testManejoException() {
        when(examRepository.findAll()).thenReturn(Datos.EXAMENES);
        when(questionRepository.findPreguntasPorExamenId(anyLong()))
                .thenThrow(IllegalArgumentException.class);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.findExamenPorNombreConPreguntas("Matematicas");
        });
        assertEquals(IllegalArgumentException.class, exception.getClass());

        verify(examRepository).findAll();
        verify(questionRepository).findPreguntasPorExamenId(anyLong());
    }

    @Test
    void testManejoExceptionNull() {
        when(examRepository.findAll()).thenReturn(Datos.EXAMENES_ID_NULL);
        when(questionRepository.findPreguntasPorExamenId(isNull()))
                .thenThrow(IllegalArgumentException.class);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.findExamenPorNombreConPreguntas("Matematicas");
        });
        assertEquals(IllegalArgumentException.class, exception.getClass());

        verify(examRepository).findAll();
        verify(questionRepository).findPreguntasPorExamenId(isNull());
    }

    @Test
    void testArgumentMatchers() {
        when(examRepository.findAll()).thenReturn(Datos.EXAMENES);
        when(questionRepository.findPreguntasPorExamenId(anyLong())).thenReturn(Datos.PREGUNTAS);
        service.findExamenPorNombreConPreguntas("Matematicas");

        verify(examRepository).findAll();
        verify(questionRepository).findPreguntasPorExamenId(
                argThat(arg -> arg != null && arg.equals(5L) && arg >= 5L));

    }

    @Test
    void testArgumentMatchers2() {
        when(examRepository.findAll()).thenReturn(Datos.EXAMENES_ID_NEGATIVOS);
        when(questionRepository.findPreguntasPorExamenId(anyLong())).thenReturn(Datos.PREGUNTAS);
        service.findExamenPorNombreConPreguntas("Historia");

        verify(examRepository).findAll();
        verify(questionRepository).findPreguntasPorExamenId(argThat(new MiArgsMatchers()));

    }

    @Test
    void testArgumentMatchers3() {
        when(examRepository.findAll()).thenReturn(Datos.EXAMENES_ID_NEGATIVOS);
        when(questionRepository.findPreguntasPorExamenId(anyLong())).thenReturn(Datos.PREGUNTAS);
        service.findExamenPorNombreConPreguntas("Historia");

        verify(examRepository).findAll();
        verify(questionRepository).findPreguntasPorExamenId(argThat((argument) -> argument != null && argument > 0));

    }

    public static class MiArgsMatchers implements ArgumentMatcher<Long> {

        private Long argument;
        @Override
        public boolean matches(Long argument) {
            this.argument = argument;
            return argument != null && argument > 0;
        }

        @Override
        public String toString() {
            return "es para un mensaje personalizado de error que imprime " +
                    "mockito en caso de que falle el test "
                    + argument + " debe ser un entero positivo";
        }
    }

    @Test
    void testArgumentCaptor() {
        when(examRepository.findAll()).thenReturn(Datos.EXAMENES);
        //when(questionRepository.findPreguntasPorExamenId(anyLong())).thenReturn(Datos.PREGUNTAS);

        service.findExamenPorNombreConPreguntas("Matematicas");
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(questionRepository).findPreguntasPorExamenId(captor.capture());

        assertEquals(5L, captor.getValue());
    }

    @Test
    void testArgumentCaptorWithAnotations() {
        when(examRepository.findAll()).thenReturn(Datos.EXAMENES);

        service.findExamenPorNombreConPreguntas("Matematicas");
        verify(questionRepository).findPreguntasPorExamenId(capture.capture());

        assertEquals(5L, capture.getValue());
    }

    @Test
    void testDoThrow() {
        Examen examen = Datos.EXAMEN;
        examen.setPreguntas(Datos.PREGUNTAS);
        doThrow(IllegalArgumentException.class).when(questionRepository).guardarVarias(anyList());

        assertThrows(IllegalArgumentException.class, () -> {
            service.guardar(examen);
        });
    }

    @Test
    void testDoAnswer() {
        when(examRepository.findAll()).thenReturn(Datos.EXAMENES);
        //when(questionRepository.findPreguntasPorExamenId(anyLong())).thenReturn(Datos.PREGUNTAS);
        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return id == 5L ? Datos.PREGUNTAS: Collections.emptyList();
        }).when(questionRepository).findPreguntasPorExamenId(anyLong());

        Examen examen = service.findExamenPorNombreConPreguntas("Matematicas");
        assertEquals(5, examen.getPreguntas().size());
        assertTrue(examen.getPreguntas().contains("geometría"));
        assertEquals(5L, examen.getId());
        assertEquals("Matematicas", examen.getNombre());

        verify(questionRepository).findPreguntasPorExamenId(anyLong());
    }

    @Test
    void testDoAnswerGuardarExamen() {
        Examen newExamen = Datos.EXAMEN;
        newExamen.setPreguntas(Datos.PREGUNTAS);

        doAnswer(new Answer<Examen>() {
            Long secuencia = 8L;

            @Override
            public Examen answer(InvocationOnMock invocationOnMock) throws Throwable {
                Examen examen = invocationOnMock.getArgument(0);
                examen.setId(secuencia++);
                return examen;
            }
        }).when(examRepository).guardar(any(Examen.class));

        // When
        Examen examen = service.guardar(newExamen);

        // Then
        assertNotNull(examen.getId());
        assertEquals(8L, examen.getId());
        assertNotEquals("Física", examen.getNombre());

        verify(examRepository).guardar(any(Examen.class));
        verify(questionRepository).guardarVarias(anyList());
    }

    @Test
    void testDoCallRealMethod() {
        when(examRepository.findAll()).thenReturn(Datos.EXAMENES);
        //when(questionRepository.findPreguntasPorExamenId(anyLong())).thenReturn(Datos.PREGUNTAS);

        doCallRealMethod().when(questionRepository).findPreguntasPorExamenId(anyLong());

        Examen examen = service.findExamenPorNombreConPreguntas("Matematicas");
        assertEquals(5L, examen.getId());
        assertEquals("Matematicas", examen.getNombre());
    }

    @Test
    void testSpy() {
        ExamenRepository examenRepository = spy(ExamenRepositoryImpl.class);
        PreguntaRepository preguntaRepository = spy(PreguntaRepositoryImpl.class);
        ExamenService examenService = new ExamenServiceImpl(examenRepository, preguntaRepository);

        List<String> preguntas = Arrays.asList("aritmética");
        //when(preguntaRepository.findPreguntasPorExamenId(anyLong())).thenReturn(preguntas);
        doReturn(preguntas).when(preguntaRepository).findPreguntasPorExamenId(anyLong());
        Examen examen = examenService.findExamenPorNombreConPreguntas("Matematicas");
        assertEquals(5, examen.getId());
        assertEquals("Matematicas", examen.getNombre());
        assertEquals(1, examen.getPreguntas().size());
        assertTrue(examen.getPreguntas().contains("aritmética"));

        verify(examenRepository).findAll();
        verify(preguntaRepository).findPreguntasPorExamenId(anyLong());
    }
}
