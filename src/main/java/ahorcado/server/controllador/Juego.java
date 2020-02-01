package ahorcado.server.controllador;

import ahorcado.server.modelo.Usuario;

import java.util.HashMap;

public class Juego {

    private static volatile HashMap<Integer, Usuario> usuariosLogueados = new HashMap<>();

    public static synchronized void anadirJugadorLogueado(Usuario usuario){
        // Añadimos al usuario a la lista de usuarios logueados
        if (!usuariosLogueados.containsKey(usuario.getId())){
            usuariosLogueados.put(usuario.getId(), usuario);
        }
    }

    public static synchronized boolean comprobarJugadorLogueado(Usuario usuario){
        // Comprobamos si un cierto usuario está logueado
        return usuariosLogueados.containsKey(usuario.getId());
    }
}
