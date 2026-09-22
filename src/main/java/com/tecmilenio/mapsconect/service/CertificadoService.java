package com.tecmilenio.mapsconect.service;

import com.tecmilenio.mapsconect.dto.AsignacionCertificadoDTO;
import com.tecmilenio.mapsconect.dto.CertificadoDTO;
import com.tecmilenio.mapsconect.dto.MateriaSimpleDTO;
import com.tecmilenio.mapsconect.entity.Certificado;
import com.tecmilenio.mapsconect.entity.CertificadoMateria;
import com.tecmilenio.mapsconect.entity.EstudianteCertificado;
import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;
import com.tecmilenio.mapsconect.repository.CertificadoMateriaRepository;
import com.tecmilenio.mapsconect.repository.CertificadoRepository;
import com.tecmilenio.mapsconect.repository.EstudianteCertificadoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de certificados académicos MAPS Connect.
 *
 * <p>Los certificados son trayectorias de aprendizaje opcionales que agrupan
 * materias de distintos semestres. Este servicio permite crear certificados,
 * vincular materias, asignar certificados a estudiantes (con un límite
 * máximo de 3 por estudiante) y listar la ruta de certificados de un
 * estudiante específico.</p>
 *
 * <p>Los nombres de materias se resuelven mediante consultas nativas al
 * {@link EntityManager} para evitar múltiples queries individuales.</p>
 */
@Service
public class CertificadoService {

    private static final int MAX_CERTIFICADOS_POR_ESTUDIANTE = 3;

    @Autowired
    private CertificadoRepository certificadoRepository;

    @Autowired
    private CertificadoMateriaRepository certificadoMateriaRepository;

    @Autowired
    private EstudianteCertificadoRepository estudianteCertificadoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<CertificadoDTO> listar() {
        return certificadoRepository.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    public CertificadoDTO obtener(Integer id) {
        Certificado certificado = certificadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificado no encontrado"));
        return mapearADTO(certificado);
    }

    @Transactional
    public CertificadoDTO crear(String nombre, String descripcion) {
        if (certificadoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException("Ya existe un certificado con ese nombre");
        }

        Certificado certificado = Certificado.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .build();

        return mapearADTO(certificadoRepository.save(certificado));
    }

    @Transactional
    public CertificadoDTO vincularMateria(Integer idCertificado, Integer idMateria) {
        Certificado certificado = certificadoRepository.findById(idCertificado)
                .orElseThrow(() -> new ResourceNotFoundException("Certificado no encontrado"));

        if (!existeMateria(idMateria)) {
            throw new IllegalArgumentException("La materia indicada no existe");
        }
        if (certificadoMateriaRepository.existsByIdCertificadoAndIdMateria(idCertificado, idMateria)) {
            throw new IllegalArgumentException("La materia ya está vinculada al certificado");
        }

        certificadoMateriaRepository.save(CertificadoMateria.builder()
                .idCertificado(idCertificado)
                .idMateria(idMateria)
                .build());

        return mapearADTO(certificado);
    }

    @Transactional
    public CertificadoDTO asignarAEstudiante(AsignacionCertificadoDTO dto) {
        Certificado certificado = certificadoRepository.findById(dto.getIdCertificado())
                .orElseThrow(() -> new ResourceNotFoundException("Certificado no encontrado"));

        if (estudianteCertificadoRepository.existsByIdEstudianteAndIdCertificado(
                dto.getIdEstudiante(), dto.getIdCertificado())) {
            throw new IllegalArgumentException("El certificado ya está asignado a este estudiante");
        }

        long actuales = estudianteCertificadoRepository.countByIdEstudiante(dto.getIdEstudiante());
        if (actuales >= MAX_CERTIFICADOS_POR_ESTUDIANTE) {
            throw new IllegalArgumentException("El estudiante ya tiene el máximo de "
                    + MAX_CERTIFICADOS_POR_ESTUDIANTE + " certificados");
        }

        estudianteCertificadoRepository.save(EstudianteCertificado.builder()
                .idEstudiante(dto.getIdEstudiante())
                .idCertificado(dto.getIdCertificado())
                .fechaSeleccion(LocalDateTime.now())
                .build());

        return mapearADTO(certificado);
    }

    public List<CertificadoDTO> listarDeEstudiante(Integer idEstudiante) {
        List<EstudianteCertificado> asignaciones =
                estudianteCertificadoRepository.findByIdEstudianteOrderByFechaSeleccionAsc(idEstudiante);

        return asignaciones.stream()
                .map(a -> certificadoRepository.findById(a.getIdCertificado())
                        .orElseThrow(() -> new ResourceNotFoundException("Certificado no encontrado")))
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private CertificadoDTO mapearADTO(Certificado certificado) {
        List<CertificadoMateria> materias =
                certificadoMateriaRepository.findByIdCertificadoOrderByIdAsc(certificado.getId());

        List<Integer> idsMaterias = materias.stream()
                .map(CertificadoMateria::getIdMateria)
                .collect(Collectors.toList());

        Map<Integer, String> nombres = obtenerNombresMaterias(idsMaterias);

        List<MateriaSimpleDTO> materiasDTO = materias.stream()
                .map(m -> MateriaSimpleDTO.builder()
                        .id(m.getIdMateria())
                        .nombre(nombres.get(m.getIdMateria()))
                        .build())
                .collect(Collectors.toList());

        return CertificadoDTO.builder()
                .id(certificado.getId())
                .nombre(certificado.getNombre())
                .descripcion(certificado.getDescripcion())
                .materias(materiasDTO)
                .build();
    }

    private Map<Integer, String> obtenerNombresMaterias(List<Integer> ids) {
        Map<Integer, String> nombres = new LinkedHashMap<>();
        if (ids == null || ids.isEmpty()) {
            return nombres;
        }

        List<Object[]> filas = entityManager
                .createNativeQuery("SELECT id_materia, nombre_materia FROM materias WHERE id_materia IN (?1)")
                .setParameter(1, ids)
                .getResultList();

        for (Object[] fila : filas) {
            nombres.put(((Number) fila[0]).intValue(), (String) fila[1]);
        }
        return nombres;
    }

    private boolean existeMateria(Integer idMateria) {
        Number conteo = (Number) entityManager
                .createNativeQuery("SELECT COUNT(*) FROM materias WHERE id_materia = ?1")
                .setParameter(1, idMateria)
                .getSingleResult();
        return conteo.intValue() > 0;
    }

}

