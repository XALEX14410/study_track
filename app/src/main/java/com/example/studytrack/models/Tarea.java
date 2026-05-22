package com.example.studytrack.models;

public class Tarea {

    private final String titulo;
    private final String descripcion;
    private final String fechaEntrega;

    public Tarea(String titulo, String descripcion, String fechaEntrega) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaEntrega = fechaEntrega;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFechaEntrega() {
        return fechaEntrega;
    }
}
