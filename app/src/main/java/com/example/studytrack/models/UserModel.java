package com.example.studytrack.models;

public class UserModel {

    private String uid;
    private String nombre;
    private String email;
    private String foto;
    private String provider;
    private long fechaRegistro;
    private long ultimoLogin;

    public UserModel() {
        // Required by Firestore.
    }

    public UserModel(String uid, String nombre, String email, String foto, String provider,
                     long fechaRegistro, long ultimoLogin) {
        this.uid = uid;
        this.nombre = nombre;
        this.email = email;
        this.foto = foto;
        this.provider = provider;
        this.fechaRegistro = fechaRegistro;
        this.ultimoLogin = ultimoLogin;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public long getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(long fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public long getUltimoLogin() {
        return ultimoLogin;
    }

    public void setUltimoLogin(long ultimoLogin) {
        this.ultimoLogin = ultimoLogin;
    }
}
