package ahorcado.server.modelo;

public class Rol {

    private int id;
    private String nombre;

    public static Rol from(Tipo tipo){
        if (tipo == Tipo.ADMIN){
            return new Rol(1,Tipo.ADMIN.name().toLowerCase());
        }

        return new Rol(2, Tipo.NORMAL.name().toLowerCase());
    }

    public Rol(){}

    public Rol(int id, String nombre){
        this.id = id;
        this.nombre = nombre;
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

    public enum Tipo {
        ADMIN,
        NORMAL
    }
}
