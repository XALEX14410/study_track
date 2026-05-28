package com.jesuslara.studytrack.models;

/**
 * Tarea pertenece al array "tareas" anidado dentro de cada materia.
 * Requiere constructor vacio y setters publicos para que Firestore pueda
 * deserializarla mediante reflexion.
 */
public class Tarea {

    private String titulo;
    private String descripcion;
    private String fechaEntrega;

    public Tarea() {
        // Requerido por Firestore.
    }

    public Tarea(String titulo, String descripcion, String fechaEntrega) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaEntrega = fechaEntrega;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(String fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }
}
