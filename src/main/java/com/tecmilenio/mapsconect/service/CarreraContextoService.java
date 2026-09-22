package com.tecmilenio.mapsconect.service;

import com.tecmilenio.mapsconect.entity.Carrera;
import com.tecmilenio.mapsconect.entity.Estudiante;
import com.tecmilenio.mapsconect.entity.PlanEstudios;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.repository.CarreraRepository;
import com.tecmilenio.mapsconect.repository.EstudianteRepository;
import com.tecmilenio.mapsconect.repository.PlanEstudiosRepository;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Contexto de carrera del usuario autenticado.
 *
 * Para un estudiante con carrera activa resuelve cuáles son sus materias
 * (plan de estudios) y permite filtrar todo el contenido académico
 * (tips, apuntes, foro, sesiones, empresas) por su carrera.
 * Para profesores y administradores devuelve null (sin filtro: ven todo).
 */
@Service
public class CarreraContextoService {

    private static final Set<String> PALABRAS_IGNORAR = Set.of(
            "en", "de", "del", "la", "las", "los", "el", "un", "una", "uno", "al", "a",
            "y", "o", "u", "e", "para", "por", "con", "entre", "sobre", "di", "b",
            "ing", "ingenieria", "ingenierias", "lic", "licenciatura", "tecnico", "tecnica",
            "profesional", "especialidad", "asociado", "desarrollo");

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private CarreraRepository carreraRepository;

    @Autowired
    private PlanEstudiosRepository planEstudiosRepository;

    /**
     * Nombre de la carrera activa del estudiante. null si no es estudiante
     * o no tiene carrera activa (en ese caso no se aplica filtro).
     */
    public String carreraDelUsuario(String email) {
        Estudiante estudiante = estudianteDelUsuario(email);
        if (estudiante == null || estudiante.getIdCarrera() == null) {
            return null;
        }
        Carrera carrera = carreraRepository.findById(estudiante.getIdCarrera()).orElse(null);
        if (carrera == null || !Boolean.TRUE.equals(carrera.getActiva())) {
            return null;
        }
        return carrera.getNombre();
    }

    /** id de la carrera activa del estudiante; null si no aplica. */
    public Integer idCarreraDeUsuario(String email) {
        Estudiante estudiante = estudianteDelUsuario(email);
        if (estudiante == null || estudiante.getIdCarrera() == null) {
            return null;
        }
        Carrera carrera = carreraRepository.findById(estudiante.getIdCarrera()).orElse(null);
        if (carrera == null || !Boolean.TRUE.equals(carrera.getActiva())) {
            return null;
        }
        return carrera.getId();
    }

    /**
     * Materias del plan de estudios de la carrera del estudiante.
     * null = sin filtro (profesor/administrador/estudiante sin carrera).
     * Vacío = es estudiante con carrera pero el plan no tiene materias.
     */
    public Set<Integer> materiasDeCarrera(String email) {
        Integer idCarrera = idCarreraDeUsuario(email);
        if (idCarrera == null) {
            return null;
        }
        Set<Integer> ids = new HashSet<>();
        for (PlanEstudios plan : planEstudiosRepository.findByCarreraId(idCarrera)) {
            ids.add(plan.getMateria().getId());
        }
        return ids;
    }

    /**
     * Nombres normalizados de las materias del plan de la carrera.
     * Se usa para cruzar contenido que guarda el nombre de la materia en texto libre.
     */
    public Set<String> nombresMateriasDeCarrera(String email) {
        Integer idCarrera = idCarreraDeUsuario(email);
        if (idCarrera == null) {
            return null;
        }
        Set<String> nombres = new HashSet<>();
        for (PlanEstudios plan : planEstudiosRepository.findByCarreraId(idCarrera)) {
            nombres.add(nombreNormalizado(plan.getMateria().getNombre()));
        }
        return nombres;
    }

    /** true si el usuario es estudiante con una carrera activa. */
    public boolean esEstudianteConCarrera(String email) {
        return idCarreraDeUsuario(email) != null;
    }

    /** true si el estudiante tiene la materia dentro del plan de su carrera. */
    public boolean materiaEsDeCarrera(String email, Integer idMateria) {
        if (email == null || idMateria == null) {
            return false;
        }
        Set<Integer> materias = materiasDeCarrera(email);
        if (materias == null) {
            return true;
        }
        return materias.contains(idMateria);
    }

    /** Verifica si un campo de carreras afines (texto libre) comparte un término con la carrera. */
    public boolean coincide(String carrerasTexto, Set<String> tokensCarrera) {
        Set<String> tokensTexto = tokensSignificativos(carrerasTexto);
        if (tokensTexto.isEmpty() || tokensCarrera == null || tokensCarrera.isEmpty()) {
            return false;
        }
        for (String token : tokensCarrera) {
            if (tokensTexto.contains(token)) {
                return true;
            }
        }
        return false;
    }

    /** Tokens significativos de un texto (sin acentos, sin abreviaturas genéricas). */
    public Set<String> tokensSignificativos(String texto) {
        Set<String> tokens = new HashSet<>();
        if (texto == null || texto.isBlank()) {
            return tokens;
        }
        String normalizado = nombreNormalizado(texto);
        for (String palabra : normalizado.split("[^a-z0-9]+")) {
            if (palabra.length() >= 3 && !PALABRAS_IGNORAR.contains(palabra)) {
                tokens.add(palabra);
            }
        }
        return tokens;
    }

    /** Nombre sin acentos y en minúsculas, para comparar nombres de materia escritos en texto libre. */
    public String nombreNormalizado(String nombre) {
        if (nombre == null) {
            return "";
        }
        return Normalizer.normalize(nombre.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim();
    }

    /** ¿El nombre de la materia coincide con alguna del plan de la carrera? (soporta versión corta o extendida). */
    public boolean materiaCoincideConPlan(String nombreMateria, Set<String> nombresPlan) {
        if (nombreMateria == null || nombresPlan == null || nombresPlan.isEmpty()) {
            return false;
        }
        String normalizada = nombreNormalizado(nombreMateria);
        if (normalizada.isEmpty()) {
            return false;
        }
        for (String delPlan : nombresPlan) {
            if (delPlan.equals(normalizada)
                    || delPlan.startsWith(normalizada)
                    || normalizada.startsWith(delPlan)) {
                return true;
            }
        }
        return false;
    }

    private Estudiante estudianteDelUsuario(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null || usuario.getRol() != Usuario.Rol.ESTUDIANTE) {
            return null;
        }
        return estudianteRepository.findByIdUsuario(usuario.getId()).orElse(null);
    }

}

