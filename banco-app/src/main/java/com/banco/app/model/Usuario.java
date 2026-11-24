package com.banco.app.model;

/** Usuario del sistema. Clase base de {@link Cliente} y {@link Gerente}. */
public abstract class Usuario {

    private final String id;
    private final String nombre;
    private final String username;
    private final String password;

    public Usuario(String id, String nombre, String username, String password) {
        this.id = id;
        this.nombre = nombre;
        this.username = username;
        this.password = password;
    }

    /**
     * Verifica las credenciales. El usuario no distingue mayúsculas ni espacios
     * sobrantes; la contraseña sí distingue mayúsculas.
     *
     * @return true si usuario y contraseña coinciden con los de este usuario
     */
    public boolean autenticar(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        return this.username.equalsIgnoreCase(username.trim()) && this.password.equals(password);
    }

    /** @return el rol del usuario; lo define cada subclase (polimorfismo). */
    public abstract Rol getRol();

    public String getId() { return id; }

    public String getNombre() { return nombre; }

    public String getUsername() { return username; }

    /** Necesario para guardar el usuario en usuarios.csv. */
    public String getPassword() { return password; }
}
