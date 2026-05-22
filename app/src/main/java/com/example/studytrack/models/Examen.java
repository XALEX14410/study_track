package com.example.studytrack.models;

public class Examen {

    private final String materia;
    private final String tema;
    private final String fechaHora;

    public Examen(String materia, String tema, String fechaHora) {
        this.materia = materia;
        this.tema = tema;
        this.fechaHora = fechaHora;
    }

    public String getMateria() {
        return materia;
    }

    public String getTema() {
        return tema;
    }

    public String getFechaHora() {
        return fechaHora;
    }
}
