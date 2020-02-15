package ahorcado.server.controlador.manejadores;

import ahorcado.server.controlador.ControlLogueados;
import ahorcado.server.controlador.ControlPartidas;
import ahorcado.server.controlador.protocolos.ProtocoloPartida;
import ahorcado.server.modelo.Partida;
import ahorcado.server.modelo.Peticion;
import ahorcado.server.modelo.Usuario;
import ahorcado.server.utils.Par;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PartidaMultijugadorManejador extends Thread implements IManejador {

    // Peticiones que podremos enrutar con este manejador
    private static final HashMap<String, ArrayList<String>> peticionesEnrutables = new HashMap<>();
    static {
        peticionesEnrutables.put("POST", new ArrayList<String>(){{
            add("crearPartidaMultijugador");
            add("obtenerPartidasMultijugador");
            add("unirsePartidaMultijugador");
        }});
    }
    private Runnable funcionAEjecutar;
    private ProtocoloPartida protocoloPartida;

    public PartidaMultijugadorManejador(){}

    @Override
    public boolean contieneRecurso(Peticion peticion) {

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

                    // Crearemos una partida multijugador
                    case "crearPartidaMultijugador":
                        funcionAEjecutar = new Runnable() {
                            @Override
                            public void run() {
                                crearPartidaMultijugador(peticion);
                            }
                        };
                        break;

                    // Obtendremos la lista de partidas disponibles
                    case "obtenerPartidasMultijugador":
                        funcionAEjecutar = new Runnable() {
                            @Override
                            public void run() {
                                obtenerPartidasMultijugador(peticion);
                            }
                        };
                        break;

                    // Nos uniremos a alguna partida ya creada
                    case "unirsePartidaMultijugador":
                        funcionAEjecutar = new Runnable() {
                            @Override
                            public void run() {
                                //nuevaPartida(peticion);
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

    private void crearPartidaMultijugador(Peticion peticion){

        JSONObject resJson;

        // Comprobamos que la petición tenga un token
        if(!peticion.getArgumentos().containsKey("token")){
            resJson = new JSONObject();
            resJson.put("codigo",400);
            resJson.put("msg","No se ha enviado ningun token adjunto a la petición");
            peticion.setRespuesta(resJson);
            peticion.finalizar();
            return;
        }

        // Obtenemos el token de los argumentos
        UUID token = UUID.fromString(peticion.getArgumentos().get("token"));
        if (!ControlLogueados.comprobarJugadorLogueado(token)){
            resJson = new JSONObject();
            resJson.put("codigo",400);
            resJson.put("msg","El usuario no esta logueado");
            peticion.setRespuesta(resJson);
            peticion.finalizar();
            return;
        }

        Usuario usuario = ControlLogueados.obtenerJugadorLogueadoPorToken(token);
        if (ControlPartidas.getInstance().estaEnPartida(usuario)){
            resJson = new JSONObject();
            resJson.put("codigo",500);
            resJson.put("msg","El jugador ya se encuentra en una partida");
            peticion.setRespuesta(resJson);
            peticion.finalizar();
            return;
        }

        Partida partida = ControlPartidas.getInstance().crearPartida(peticion,usuario);
        // No se ha podido crear la partida, finalizamos la conexión
        if (partida == null){
            resJson = new JSONObject();
            resJson.put("codigo",500);
            resJson.put("msg","No se ha podido crear la partida");
            peticion.setRespuesta(resJson);
            peticion.finalizar();
            return;
        }

        // Enviamos la respuesta pero no matamos la conexión
        else {

            resJson = new JSONObject();
            resJson.put("codigo",200);
            resJson.put("msg","Se ha creado una partida. El codigo de la sala es \'" + partida.getId() + "\'. Esperando a que se unan otros usuarios...");
            peticion.enviarMensaje(resJson);
            return;
        }
    }

    private void obtenerPartidasMultijugador(Peticion peticion){

        JSONObject resJson;

        // Comprobamos que la petición tenga un token
        if(!peticion.getArgumentos().containsKey("token")){
            resJson = new JSONObject();
            resJson.put("codigo",400);
            resJson.put("msg","No se ha enviado ningun token adjunto a la petición");
            peticion.setRespuesta(resJson);
            peticion.finalizar();
            return;
        }

        // Obtenemos el token de los argumentos
        UUID token = UUID.fromString(peticion.getArgumentos().get("token"));
        if (!ControlLogueados.comprobarJugadorLogueado(token)){
            resJson = new JSONObject();
            resJson.put("codigo",400);
            resJson.put("msg","El usuario no esta logueado");
            peticion.setRespuesta(resJson);
            peticion.finalizar();
            return;
        }

        // Enviamos la lista de partidas disponibles
        ArrayList<Partida> partidasEmpezadas = ControlPartidas.getInstance().getPartidasEsperando();
        JSONArray partidas = new JSONArray();
        for (Partida partida : partidasEmpezadas){
            JSONObject partidaJson = new JSONObject();
            partidaJson.put("id", partida.getId());
            partidaJson.put("numJugadores", partida.getNumeroJugadores());
            partidas.add(partidaJson);

        }
        resJson = new JSONObject();
        resJson.put("codigo",200);
        resJson.put("msg", "");
        resJson.put("partidas", partidas);

        System.out.println("La respuesta a enviar es: " + resJson.toJSONString());

        peticion.setRespuesta(resJson);
        peticion.finalizar();
        return;
    }

}