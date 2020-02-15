package ahorcado.server.controlador.manejadores;

import ahorcado.server.controlador.ControlLogueados;
import ahorcado.server.modelo.Peticion;
import ahorcado.server.modelo.Rol;
import ahorcado.server.modelo.Usuario;
import ahorcado.server.utils.Metodo;
import ahorcado.server.utils.Par;
import ahorcado.server.utils.Utils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class UsuariosManejador extends Thread implements IManejador {

    public static final HashMap<String, ArrayList<String>> peticionesEnrutables = new HashMap<>();
    private Session session;
    private Runnable funcionAEjecutar;

    static {
        peticionesEnrutables.put("POST", new ArrayList<String>(){{
            add("registro");
            add("login");
            add("logout");
        }});
    }


    public UsuariosManejador(){
        this.session = Utils.obtenerSession();
    }


    @Override
    public final boolean contieneRecurso(Peticion peticion){

        String metodo = peticion.getMetodo().name();

        // Comprobamos que manejemos alguno de los métodos solicitados y, dependiendo de cada método,
        // una determinada acción
        if (peticionesEnrutables.containsKey(metodo)){
            if (peticionesEnrutables.get(metodo).contains(peticion.getAccion())){
                return true;
            }
        }
        return false;
    }

    @Override
    public void manejarConexionEntrante(Peticion peticion) {

        switch (peticion.getMetodo()){

            // Acciones con el método POST
            case POST:
                switch (peticion.getAccion()){
                    case "registro":
                        funcionAEjecutar = new Runnable() {
                            @Override
                            public void run() {
                                registrar(peticion);
                            }
                        };
                        break;

                    case "login":
                        funcionAEjecutar = new Runnable() {
                            @Override
                            public void run() {
                                login(peticion);
                            }
                        };
                        break;

                    case "logout":
                        funcionAEjecutar = new Runnable() {
                            @Override
                            public void run() {
                                logout(peticion);
                            }
                        };
                        break;
                }
                break;
        }

        // Ejecutamos la acción en un hilo nuevo
        this.start();
    }

    @Override
    public void run() {
        funcionAEjecutar.run();
    }


    public void registrar(Peticion peticion){

        JSONObject respuesta = new JSONObject();

        // Comprobamos que la petición contenga los campos necesarios
        if (!contieneCamposNecesariosRegistro(peticion)){
            respuesta.put("codigo",400);
            respuesta.put("msg", "La petición no contiene los campos necesarios");
            peticion.setRespuesta(respuesta);
            peticion.finalizar();
            return;
        }

        // Comprobamos que los valores de los argumentos sean válidos (tamanio contrasenia... etc)
        if (!comprobarValoresCorrectosRegistro(peticion.getArgumentos())){
            respuesta.put("codigo","400");
            respuesta.put("msg", "Los argumentos no cumplen con los requisitos");
            peticion.setRespuesta(respuesta);
            peticion.finalizar();
            return;
        }

        Transaction transaction = null;

        try{

            // Comenzamos una transacción
            transaction = session.beginTransaction();

            // Obtenemos los campos importantes del usuario
            HashMap<String, String> args = peticion.getArgumentos();
            String nombre = args.get("nombre");
            String contrasenia = args.get("contrasenia");

            // Creamos el usuario con los datos requeridos
            Usuario usuario = new Usuario(nombre, contrasenia, Rol.from(Rol.Tipo.NORMAL));

            // Guardamos al empleado
            session.save(usuario);

            // Guardamos punto de control
            transaction.commit();

        }catch (Exception e){
            if (transaction != null){
                transaction.rollback();
            }
            e.printStackTrace();
            peticion.finalizar();
        }

        // Creamos la respuesta
        respuesta.put("codigo",200);
        respuesta.put("msg","");
        peticion.setRespuesta(respuesta);
        peticion.finalizar();

    }

    public void login(Peticion peticion){

        JSONObject respuesta = new JSONObject();

        // Comprobamos que la petición contenga los datos necesario
        if (!contieneCamposNecesariosLoguin(peticion)){
            respuesta.put("codigo",400);
            respuesta.put("msg", "La petición no contiene los campos necesarios");
            peticion.setRespuesta(respuesta);
            peticion.finalizar();
            return;
        }

        Transaction transaction = null;

        try{

            String nombreUsuario = peticion.getArgumentos().get("nombre");
            String contrasenia = peticion.getArgumentos().get("contrasenia");

            // Comenzamos una transacción
            transaction = session.beginTransaction();

            Query query = session.createQuery("FROM Usuario WHERE nombre = :nombre AND contrasenia = :contrasenia");
            query.setParameter("nombre", nombreUsuario);
            query.setParameter("contrasenia", contrasenia);

            Usuario usuario = (Usuario) query.getSingleResult();

            // Guardamos punto de control
            transaction.commit();

            if (usuario != null){

                // Creamos el token que tendrá el usuario para realizar las siguientes peticiones como
                // jugador logueado
                UUID token = Utils.generarUUID();

                Par usuarioToken = new Par(usuario,token);

                // Guardamos el login del usuario
                boolean res = ControlLogueados.anadirJugadorLogueado(usuarioToken);
                if (res){
                    respuesta.put("codigo","200");
                    respuesta.put("msg","");
                    respuesta.put("token", token.toString());
                    peticion.setRespuesta(respuesta);
                    peticion.finalizar();
                }else {
                    respuesta.put("codigo","400");
                    respuesta.put("msg","No se ha podido hacer el login");
                    peticion.setRespuesta(respuesta);
                    peticion.finalizar();
                }
            }

            // No se ha encontrado ningún usuario con esa combinación
            else {
                respuesta.put("codigo",404);
                respuesta.put("msg","No se ha encontrado ningún usuario con esa combinación");
                peticion.setRespuesta(respuesta);;
                peticion.finalizar();
            }

        }catch (Exception e){
            if (transaction != null){
                transaction.rollback();
            }
            e.printStackTrace();
            peticion.finalizar();
        }
    }

    public void logout(Peticion peticion){
        JSONObject respuesta = new JSONObject();

        // Comprobamos que la petición contenga los datos necesario
        if (!contieneCamposNecesariosLogout(peticion)){
            respuesta.put("codigo",400);
            respuesta.put("msg", "La peticion no contiene los campos necesarios");
            peticion.setRespuesta(respuesta);
            peticion.finalizar();
            return;
        }

        Transaction transaction = null;

        try{

            String nombreUsuario = peticion.getArgumentos().get("nombre");

            // Comenzamos una transacción
            transaction = session.beginTransaction();

            Query query = session.createQuery("FROM Usuario WHERE nombre = :nombre");
            query.setParameter("nombre", nombreUsuario);

            Usuario usuario = (Usuario) query.getSingleResult();

            // Guardamos punto de control
            transaction.commit();

            if (usuario != null && ControlLogueados.comprobarJugadorLogueado(usuario)){
                // Deslogueamos al usuario
                ControlLogueados.eliminarJugadorLogueado(usuario);
                respuesta.put("codigo","200");
                respuesta.put("msg","");
                peticion.setRespuesta(respuesta);
                peticion.finalizar();
            }

            // No se ha encontrado ningún usuario con esa combinación
            else {
                respuesta.put("codigo",404);
                respuesta.put("msg","No se ha encontrado ningun usuario con esa combinación o el usuario no se ha logueado aún");
                peticion.setRespuesta(respuesta);;
                peticion.finalizar();
            }

        }catch (Exception e){
            if (transaction != null){
                transaction.rollback();
            }
            e.printStackTrace();
            peticion.finalizar();
        }
    }


    private boolean contieneCamposNecesariosRegistro(Peticion peticion){

        if (peticion == null) return false;

        // El método tiene que ser POST
        if (peticion.getMetodo() != Metodo.POST){
            return false;
        }

        // Tiene que tener en los argumentos un nombre y una contrasenia
        HashMap<String, String> args = peticion.getArgumentos();
        if (!args.containsKey("nombre") || !args.containsKey("contrasenia")){
            return false;
        }

        return true;
    }

    private boolean comprobarValoresCorrectosRegistro(HashMap<String, String> args) {

        String nombreUsuario = args.get("nombre");
        String contrasenia = args.get("contrasenia");

        if(nombreUsuario.length() < 3) return false;
        if (contrasenia.length() < 3) return false;

        return true;
    }


    private boolean contieneCamposNecesariosLoguin(Peticion peticion){

        if (peticion == null) return false;

        // El método tiene que ser POST
        if (peticion.getMetodo() != Metodo.POST){
            return false;
        }

        // Tiene que tener en los argumentos un nombre y una contrasenia
        HashMap<String, String> args = peticion.getArgumentos();
        if (!args.containsKey("nombre") || !args.containsKey("contrasenia")){
            return false;
        }

        return true;
    }


    private boolean contieneCamposNecesariosLogout(Peticion peticion){

        if (peticion == null) return false;

        // El método tiene que ser POST
        if (peticion.getMetodo() != Metodo.POST){
            return false;
        }

        // Tiene que tener en los argumentos un nombre y una contrasenia
        HashMap<String, String> args = peticion.getArgumentos();
        if (!args.containsKey("nombre")){
            return false;
        }

        return true;
    }
}
