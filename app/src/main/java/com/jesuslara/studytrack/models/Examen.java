package com.jesuslara.studytrack.models;

/**
 * Examen pertenece al array "examenes" anidado dentro de cada materia.
 * Requiere constructor vacio y setters publicos para que Firestore pueda
 * deserializarlo mediante reflexion.
 */
public class Examen {

    private String materia;
    private String tema;
    private String fechaHora;

    public Examen() {
        // Requerido por Firestore.
    }

    public Examen(String materia, String tema, String fechaHora) {
        this.materia = materia;
        this.tema = tema;
        this.fechaHora = fechaHora;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }
}
