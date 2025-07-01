package com.example.agenda_t;

public class Entrada {
    private String key;
    private String fechaIngreso;
    private String titulo;
    private String descripcion;
    private String archivoAdjuntoUrl;
    public Entrada() {
    }

    public Entrada(String key, String fechaIngreso, String titulo, String descripcion, String archivoAdjuntoUrl) {
        this.key = key;
        this.fechaIngreso = fechaIngreso;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.archivoAdjuntoUrl = archivoAdjuntoUrl;
    }

    // Getters y setters

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(String fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
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

    public String getArchivoAdjuntoUrl() {
        return archivoAdjuntoUrl;
    }

    public void setArchivoAdjuntoUrl(String archivoAdjuntoUrl) {
        this.archivoAdjuntoUrl = archivoAdjuntoUrl;
    }
}
