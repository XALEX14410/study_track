package com.jesuslara.studytrack.models;

/**
 * Modelo base de tarea para la fase visual del modulo.
 * Incluye constructor vacio y setters publicos para serializacion en Firestore.
 */
public class Tarea {

    private String id;
    private String nombre;
    private String descripcion;
    private String fechaEntrega;
    private String horaEntrega;
    private Long timestampEntrega;
    private String materiaId;
    private String materiaNombre;
    private boolean completada;
    private String prioridad;
    private String antelacion;
    private Long createdAt;
    private Long updatedAt;

    public Tarea() {
        // Requerido por Firestore
    }

    public Tarea(
            String id,
            String nombre,
            String descripcion,
            String fechaEntrega,
            String horaEntrega,
            Long timestampEntrega,
            String materiaId,
            String materiaNombre,
            boolean completada,
            String prioridad,
            String antelacion,
            Long createdAt,
            Long updatedAt
    ) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaEntrega = fechaEntrega;
        this.horaEntrega = horaEntrega;
        this.timestampEntrega = timestampEntrega;
        this.materiaId = materiaId;
        this.materiaNombre = materiaNombre;
        this.completada = completada;
        this.prioridad = prioridad;
        this.antelacion = antelacion;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructor auxiliar para datos de demo y compatibilidad con codigo previo.
    public Tarea(String nombre, String descripcion, String fechaEntrega) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaEntrega = fechaEntrega;
        this.completada = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public String getHoraEntrega() {
        return horaEntrega;
    }

    public void setHoraEntrega(String horaEntrega) {
        this.horaEntrega = horaEntrega;
    }

    public Long getTimestampEntrega() {
        return timestampEntrega;
    }

    public void setTimestampEntrega(Long timestampEntrega) {
        this.timestampEntrega = timestampEntrega;
    }

    public String getMateriaId() {
        return materiaId;
    }

    public void setMateriaId(String materiaId) {
        this.materiaId = materiaId;
    }

    public String getMateriaNombre() {
        return materiaNombre;
    }

    public void setMateriaNombre(String materiaNombre) {
        this.materiaNombre = materiaNombre;
    }

    public boolean isCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getAntelacion() {
        return antelacion;
    }

    public void setAntelacion(String antelacion) {
        this.antelacion = antelacion;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Compatibilidad con nombres usados en codigo legado.
    public String getTitulo() {
        return nombre;
    }

    public void setTitulo(String titulo) {
        this.nombre = titulo;
    }
}
