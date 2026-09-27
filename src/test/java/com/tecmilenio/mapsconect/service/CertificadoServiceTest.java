package com.tecmilenio.mapsconect.service;

import com.tecmilenio.mapsconect.dto.AsignacionCertificadoDTO;
import com.tecmilenio.mapsconect.dto.CertificadoDTO;
import com.tecmilenio.mapsconect.entity.Certificado;
import com.tecmilenio.mapsconect.entity.CertificadoMateria;
import com.tecmilenio.mapsconect.entity.EstudianteCertificado;
import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;
import com.tecmilenio.mapsconect.repository.CertificadoMateriaRepository;
import com.tecmilenio.mapsconect.repository.CertificadoRepository;
import com.tecmilenio.mapsconect.repository.EstudianteCertificadoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificadoServiceTest {

    @Mock
    private CertificadoRepository certificadoRepository;

    @Mock
    private CertificadoMateriaRepository certificadoMateriaRepository;

    @Mock
    private EstudianteCertificadoRepository estudianteCertificadoRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private CertificadoService certificadoService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(certificadoService, "entityManager", entityManager);
    }

    @Test
    void crear_conNombreDuplicado_lanzaIllegalArgumentException() {
        when(certificadoRepository.existsByNombreIgnoreCase("Ciberseguridad")).thenReturn(true);

        assertThatThrownBy(() -> certificadoService.crear("Ciberseguridad", "Desc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un certificado con ese nombre");
    }

    @Test
    void crear_exitoso_devuelveCertificadoDTO() {
        when(certificadoRepository.existsByNombreIgnoreCase("DevOps")).thenReturn(false);
        Certificado certificadoGuardado = Certificado.builder().id(1).nombre("DevOps").descripcion("Desc").build();
        when(certificadoRepository.save(any(Certificado.class))).thenReturn(certificadoGuardado);
        when(certificadoMateriaRepository.findByIdCertificadoOrderByIdAsc(1)).thenReturn(List.of());

        CertificadoDTO dto = certificadoService.crear("DevOps", "Desc");

        assertThat(dto.getNombre()).isEqualTo("DevOps");
    }

    @Test
    void obtener_conIdInexistente_lanzaResourceNotFoundException() {
        when(certificadoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificadoService.obtener(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Certificado no encontrado");
    }

    @Test
    void asignarAEstudiante_cuandoYaTieneElCertificado_lanzaIllegalArgumentException() {
        Certificado certificado = Certificado.builder().id(1).build();
        when(certificadoRepository.findById(1)).thenReturn(Optional.of(certificado));

        when(estudianteCertificadoRepository.existsByIdEstudianteAndIdCertificado(100, 1)).thenReturn(true);

        AsignacionCertificadoDTO dto = new AsignacionCertificadoDTO();
        dto.setIdEstudiante(100);
        dto.setIdCertificado(1);

        assertThatThrownBy(() -> certificadoService.asignarAEstudiante(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El certificado ya está asignado a este estudiante");
    }

    @Test
    void asignarAEstudiante_cuandoYaTiene3Certificados_lanzaIllegalArgumentException() {
        Certificado certificado = Certificado.builder().id(1).build();
        when(certificadoRepository.findById(1)).thenReturn(Optional.of(certificado));

        when(estudianteCertificadoRepository.existsByIdEstudianteAndIdCertificado(100, 1)).thenReturn(false);
        when(estudianteCertificadoRepository.countByIdEstudiante(100)).thenReturn(3L);

        AsignacionCertificadoDTO dto = new AsignacionCertificadoDTO();
        dto.setIdEstudiante(100);
        dto.setIdCertificado(1);

        assertThatThrownBy(() -> certificadoService.asignarAEstudiante(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El estudiante ya tiene el máximo de 3 certificados");
    }

    @Test
    void asignarAEstudiante_exitoso_guardaYDevuelveCertificado() {
        Certificado certificado = Certificado.builder().id(1).nombre("Test").build();
        when(certificadoRepository.findById(1)).thenReturn(Optional.of(certificado));
        when(estudianteCertificadoRepository.existsByIdEstudianteAndIdCertificado(100, 1)).thenReturn(false);
        when(estudianteCertificadoRepository.countByIdEstudiante(100)).thenReturn(1L);
        when(certificadoMateriaRepository.findByIdCertificadoOrderByIdAsc(1)).thenReturn(List.of());

        AsignacionCertificadoDTO request = new AsignacionCertificadoDTO();
        request.setIdEstudiante(100);
        request.setIdCertificado(1);

        CertificadoDTO dto = certificadoService.asignarAEstudiante(request);
        assertThat(dto.getId()).isEqualTo(1);
    }

    @Test
    void vincularMateria_conMateriaYaVinculada_lanzaIllegalArgumentException() {
        Certificado certificado = Certificado.builder().id(1).build();
        when(certificadoRepository.findById(1)).thenReturn(Optional.of(certificado));

        jakarta.persistence.Query mockQuery = Mockito.mock(jakarta.persistence.Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyInt(), any())).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenReturn(1L);

        when(certificadoMateriaRepository.existsByIdCertificadoAndIdMateria(1, 10)).thenReturn(true);

        assertThatThrownBy(() -> certificadoService.vincularMateria(1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La materia ya está vinculada al certificado");
    }
}
