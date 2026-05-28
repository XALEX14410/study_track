package com.jesuslara.studytrack.models;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una materia dentro del array "materias" almacenado en el documento
 * usuarios/{uid} de Firestore.
 *
 * Necesita constructor vacio y setters publicos porque Firestore realiza la
 * deserializacion mediante reflexion sobre POJOs.
 */
public class Materia {

    public static final String ESTADO_ACTIVA = "activa";
    public static final String ESTADO_INACTIVA = "inactiva";

    private String idMateria;
    private String nombre;
    private String aula;
    private String profesor;
    private String estado;
    private List<Examen> examenes;
    private List<Tarea> tareas;

    public Materia() {
        // Requerido por Firestore.
        this.examenes = new ArrayList<>();
        this.tareas = new ArrayList<>();
    }

    public Materia(String idMateria,
                   String nombre,
                   String aula,
                   String profesor,
                   String estado,
                   List<Examen> examenes,
                   List<Tarea> tareas) {
        this.idMateria = idMateria;
        this.nombre = nombre;
        this.aula = aula;
        this.profesor = profesor;
        this.estado = estado;
        this.examenes = examenes != null ? examenes : new ArrayList<>();
        this.tareas = tareas != null ? tareas : new ArrayList<>();
    }

    public String getIdMateria() {
        return idMateria;
    }

    public void setIdMateria(String idMateria) {
        this.idMateria = idMateria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getAula() {
        return aula;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }

    public String getProfesor() {
        return profesor;
    }

    public void setProfesor(String profesor) {
        this.profesor = profesor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<Examen> getExamenes() {
        return examenes;
    }

    public void setExamenes(List<Examen> examenes) {
        this.examenes = examenes != null ? examenes : new ArrayList<>();
    }

    public List<Tarea> getTareas() {
        return tareas;
    }

    public void setTareas(List<Tarea> tareas) {
        this.tareas = tareas != null ? tareas : new ArrayList<>();
    }

    @Exclude
    public boolean isActiva() {
        return ESTADO_ACTIVA.equalsIgnoreCase(estado);
    }
}
