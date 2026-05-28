package com.jesuslara.studytrack.models;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserModel {

    private String uid;
    private String nombre;
    private String email;
    private String foto;
    private String provider;
    private long fechaRegistro;
    private Timestamp ultimoLogin;
    private List<Materia> materias;

    public UserModel() {
        // Required by Firestore.
        this.materias = new ArrayList<>();
    }

    public UserModel(String uid, String nombre, String email, String foto, String provider,
                     long fechaRegistro, long ultimoLogin) {
        this.uid = uid;
        this.nombre = nombre;
        this.email = email;
        this.foto = foto;
        this.provider = provider;
        this.fechaRegistro = fechaRegistro;
        this.ultimoLogin = timestampFromMillis(ultimoLogin);
        this.materias = new ArrayList<>();
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

    public Timestamp getUltimoLogin() {
        return ultimoLogin;
    }

    public void setUltimoLogin(Timestamp ultimoLogin) {
        this.ultimoLogin = ultimoLogin;
    }

    public long getUltimoLoginMillis() {
        return ultimoLogin != null ? ultimoLogin.toDate().getTime() : 0L;
    }

    public void setUltimoLoginMillis(long ultimoLoginMillis) {
        this.ultimoLogin = timestampFromMillis(ultimoLoginMillis);
    }

    public List<Materia> getMaterias() {
        return materias;
    }

    public void setMaterias(List<Materia> materias) {
        this.materias = materias != null ? materias : new ArrayList<>();
    }

    private Timestamp timestampFromMillis(long millis) {
        return millis > 0L ? new Timestamp(new Date(millis)) : null;
    }
}
