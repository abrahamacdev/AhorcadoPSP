package ahorcado.server.controlador;

import ahorcado.server.modelo.Usuario;
import ahorcado.server.utils.Par;

import java.util.HashMap;
import java.util.UUID;

public class ControlLogueados {

    private static volatile HashMap<UUID, Usuario> usuariosLogueados = new HashMap<>();

    public static synchronized boolean anadirJugadorLogueado(Par parUsuarioToken){

        Usuario usuario = null;
        UUID token = null;

        boolean primeroEsNulo = parUsuarioToken.primero == null;
        boolean segundoEsNulo = parUsuarioToken.segundo == null;

        // Comprobamos ninguno de los campos del par sean nulos
        if (!primeroEsNulo && !segundoEsNulo){

            // Obtenemos el usuario, si no hay, no hacemos nada
            if (parUsuarioToken.primero instanceof  Usuario){
                usuario = (Usuario) parUsuarioToken.primero;
            }
            else if (parUsuarioToken.segundo instanceof Usuario){
                usuario = (Usuario) parUsuarioToken.segundo;
            }
            else {
                return false;
            }

            // Obtenemos el token, si no hay, no hacemos nada
            if (parUsuarioToken.primero instanceof UUID){
                token = (UUID) parUsuarioToken.primero;
            }
            else if (parUsuarioToken.segundo instanceof UUID){
                token = (UUID) parUsuarioToken.segundo;
            }
            else {
                return false;
            }
        }

        // Añadimos al usuario a la lista de usuarios logueados
        if (!usuariosLogueados.containsKey(token)){

            System.out.println("Añadimos al usuario \'" + usuario.getNombre() + "\' a la lista de usuarios " +
                    "logueados");

            usuariosLogueados.put(token, usuario);
            return true;
        }
        return false;
    }

    public static synchronized  void eliminarJugadorLogueado(Usuario usuario){
        // Comprobamos primero que el usuario esté logueado y
        // lo eliminamos del map
        UUID uuid = comprobarJugadorLogueadoUUID(usuario);
        if (uuid != null){
            usuariosLogueados.remove(uuid);
            System.out.println("El usuario \'" + usuario.getNombre() + "\' se acaba de desloguear!!");
        }
    }

    public static synchronized boolean comprobarJugadorLogueado(Usuario usuario){
        // Comprobamos si un cierto usuario está logueado
        for (Usuario usuarioLogueado : usuariosLogueados.values()){
            if (usuarioLogueado.getId() == usuario.getId()){
                return true;
            }
        }
        return false;
    }

    public static synchronized boolean comprobarJugadorLogueado(UUID token){

        // Comprobamos si un cierto usuario está logueado
        for (UUID uuid : usuariosLogueados.keySet()){
            if (uuid.equals(token)){
                return true;
            }
        }
        return false;

    }

    public static synchronized Usuario obtenerJugadorLogueadoPorToken(UUID token){

        if (usuariosLogueados.containsKey(token)){
            return usuariosLogueados.get(token);
        }
        return null;
    }

    public static synchronized UUID comprobarJugadorLogueadoUUID(Usuario usuario){
        // Comprobamos si un cierto usuario está logueado
        for (UUID uuid : usuariosLogueados.keySet()){
            Usuario temp = usuariosLogueados.get(uuid);

            if (temp.getId() == usuario.getId()){
                return uuid;
            }
        }
        return null;
    }
}
