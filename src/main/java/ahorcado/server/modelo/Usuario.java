package ahorcado.server.modelo;

public class Usuario {

    private int id;
    private String nombre;
    private String contrasenia;
    private Rol rol;

    public Usuario(){}

    public Usuario(String nombre, String contrasenia, Rol rol){
        this.nombre = nombre;
        this.contrasenia = contrasenia;
        this.rol = rol;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (! (obj instanceof Usuario)) return false;

        Usuario usuario = (Usuario) obj;
        return usuario.getId() == id;
    }

    @Override
    public String toString() {
        return "El usuario \'" + nombre + "\' tiene el rol de usuario \'" + rol.getNombre() + "\'";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
