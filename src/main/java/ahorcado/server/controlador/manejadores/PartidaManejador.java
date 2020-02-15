package ahorcado.server.controlador.manejadores;

import ahorcado.server.controlador.ControlLogueados;
import ahorcado.server.controlador.manejadores.IManejador;
import ahorcado.server.controlador.protocolos.ProtocoloPartida;
import ahorcado.server.modelo.Peticion;
import ahorcado.server.utils.Constantes;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.naming.ldap.Control;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PartidaManejador extends Thread implements IManejador {

    // Peticiones que podremos enrutar con este manejador
    private static final HashMap<String, ArrayList<String>> peticionesEnrutables = new HashMap<>();
    static {
        peticionesEnrutables.put("POST", new ArrayList<String>(){{
            add("nuevaPartida");
        }});
    }
    private Runnable funcionAEjecutar;

    private ProtocoloPartida protocoloPartida;

    public PartidaManejador(){}

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
                    case "nuevaPartida":
                        funcionAEjecutar = new Runnable() {
                            @Override
                            public void run() {
                                nuevaPartida(peticion);
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

    public void nuevaPartida(Peticion peticion){

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
            resJson.put("msg","No se ha enviado ningun token adjunto a la petición");
            peticion.setRespuesta(resJson);
            peticion.finalizar();
            return;
        }

        // Guardamos la conexión con el cliente
        Socket socket = (Socket) peticion.getPeticionCliente();

        // Obtenemos los flujos del socket
        BufferedReader bufferedReader = null;
        PrintWriter printWriter = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Creamos un protocolo de partida
        protocoloPartida = new ProtocoloPartida();

        // Al principio le enviaremos solo la palabra
        HashMap<String, Object> resJugada = protocoloPartida.jugar(null);
        String msg = elaborarMensajeResumen(resJugada);
        resJson = elaborarRespuesta(msg, resJugada);
        printWriter.println(resJson);
        boolean partidaFinalizada = false;

        // Mientras que la partida esté activa seguimos jugando
        while (!partidaFinalizada){

            // Leemos la palabra que envíe el cliente y jugamos
            String resCliente = leerDatosFlujo(bufferedReader);
            String palabraParseada = obtenerPalabraDelCliente(resCliente);
            resJugada = protocoloPartida.jugar(palabraParseada);
            msg = elaborarMensajeResumen(resJugada);
            resJson = elaborarRespuesta(msg, resJugada);

            // Enviamos una respuesta
            printWriter.println(resJson.toJSONString());

            partidaFinalizada = (int) resJugada.get("estado") == Constantes.ProtocoloPartida.FIN;
        }


        // Cerramos los flujos
        try {
            printWriter.close();
            bufferedReader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String leerDatosFlujo(BufferedReader bufferedReader){

        try {
            return bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String obtenerPalabraDelCliente(String raw){

        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(raw);
            return (String) jsonObject.get("palabra");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String elaborarMensajeResumen(HashMap<String, Object> res){

        boolean partidaFinalizada = (int) res.get("estado") == Constantes.ProtocoloPartida.FIN;
        String resumen = (String) res.get("resumen");
        String palabraCompleta = (String) res.get("palabraCompleta");
        String palabraIncompleta = (String) res.get("palabraIncompleta");
        boolean acertado = (boolean) res.get("acertado");
        int numeroVidas = (int) res.get("vidas");

        String msg = "";

        //Comprobamos si la partida ha finalizado
        if (partidaFinalizada){
            // No le quedan vidas, ha perdido
            if (numeroVidas < 0){
                msg = "Has perdido :(";
            }
            // Ha ganado
            else {
                msg = "Enhorabuena, has ganado!!. La palabra correcta era \'" + palabraCompleta + "\'";
            }
        }

        // Aún no ha acabado
        else {

            // Comprobamos si es la primera vez(sólo le enviaremos la palabra incompleta)
            if (!acertado && numeroVidas == Constantes.ProtocoloPartida.NUMERO_VIDAS){
                msg = "La palabra es \'" + palabraIncompleta + "\'";
            }

            // Ha fallado
            else if (!acertado){
                msg = "Has fallado. Te quedan " + numeroVidas + " vidas\n" +
                        "Palabra: \'" + palabraIncompleta + "\'";
            }

            // Ha acertado con la letra/palabra
            else {
                msg = "Palabra: \'" + palabraIncompleta + "\'";
            }
        }

        // Añadimos el dibujo del muñeco
        msg += "\n" + resumen;

        return msg;
    }

    private JSONObject elaborarRespuesta(String msg, HashMap<String, Object> resJugada){

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("codigo", 200);
        jsonObject.put("msg", msg);
        jsonObject.put("vidas", resJugada.get("vidas"));
        jsonObject.put("finalizada", (int) resJugada.get("estado") == Constantes.ProtocoloPartida.FIN);

        return jsonObject;
    }
}
