package ahorcado.server.utils;

public class Par {

    public final Object primero;
    public final Object segundo;

    public Par (Object primero, Object segundo){
        this.primero = primero;
        this.segundo = segundo;
    }

    @Override
    public String toString() {
        return "Primero: " + primero + " -- Segundo: " + segundo;
    }
}
