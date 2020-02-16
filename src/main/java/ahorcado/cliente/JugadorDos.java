package ahorcado.cliente;

import ahorcado.server.utils.Constantes;
import ahorcado.server.utils.Metodo;
import ahorcado.server.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;

public class JugadorDos {

    public static void main(String[] args){

        JugadorDos jugadorDos = new JugadorDos();

        /* --- UDP --- */
        // Registramos un nuevo usuario
        //jugadorDos.realizarRegistroCliente();

        // Nos logueamos/deslogueamos con el usuario existente
        String token = jugadorDos.realizarLoginCliente();
        //jugadorDos.realizarLogoutCliente();
        /* ----------- */




        /* --- TCP --- */
        jugadorDos.mostrarPartidasDisponibles(token);
        jugadorDos.unirnosAPartida(token);
        /* ----------- */
    }



    public void realizarRegistroCliente(){

        InetAddress inetAddress = obtenerDireccionLocal();

        JSONObject jsonPeticion = new JSONObject();
        jsonPeticion.put("metodo", Metodo.POST.name());
        jsonPeticion.put("accion","registro");

        JSONObject jsonArgs = new JSONObject();
        jsonArgs.put("nombre","Juan");
        jsonArgs.put("contrasenia","1234");

        jsonPeticion.put("args", jsonArgs);

        JSONObject jsonRes = realizarPeticionUDP(inetAddress, Constantes.PUERTO_UDP, jsonPeticion);

        if (jsonRes != null){
            System.out.println("Respuesta del servidor a nuestra petición de registrarnos: " + jsonRes.toJSONString());
        }

    }

    public String realizarLoginCliente(){

        InetAddress inetAddress = obtenerDireccionLocal();

        JSONObject jsonPeticion = new JSONObject();
        jsonPeticion.put("metodo",Metodo.POST.name());
        jsonPeticion.put("accion","login");

        JSONObject jsonArgs = new JSONObject();
        jsonArgs.put("nombre","Juan");
        jsonArgs.put("contrasenia","1234");

        jsonPeticion.put("args", jsonArgs);

        JSONObject jsonRes = realizarPeticionUDP(inetAddress, Constantes.PUERTO_UDP, jsonPeticion);

        if (jsonRes != null){
            System.out.println("Respuesta del servidor a nuestra petición de loguearnos: " + jsonRes.toJSONString());
            return (String) jsonRes.get("token");
        }
        return null;
    }

    public void realizarLogoutCliente(){

        InetAddress inetAddress = obtenerDireccionLocal();

        JSONObject jsonPeticion = new JSONObject();
        jsonPeticion.put("metodo",Metodo.POST.name());
        jsonPeticion.put("accion","logout");

        JSONObject jsonArgs = new JSONObject();
        jsonArgs.put("nombre","Juan");

        jsonPeticion.put("args", jsonArgs);

        JSONObject jsonRes = realizarPeticionUDP(inetAddress, Constantes.PUERTO_UDP, jsonPeticion);

        if (jsonRes != null){
            System.out.println("Respuesta del servidor a nuestra petición de loguearnos: " + jsonRes.toJSONString());
        }
    }

    public void mostrarPartidasDisponibles(String token){

        Socket conexion = crearClientSocket();
        PrintWriter printWriter = obtenerPrintWriterSocket(conexion);
        BufferedReader bufferedReader = obtenerBufferedReaderSocket(conexion);

        JSONObject args = new JSONObject();
        args.put("token", token);

        JSONObject datosPeticion = new JSONObject();
        datosPeticion.put("metodo", Metodo.POST.name());
        datosPeticion.put("accion", "obtenerPartidasMultijugador");
        datosPeticion.put("args", args);

        // Enviamos los datos de la petición
        printWriter.println(datosPeticion.toJSONString());

        // Recibimos la respuesta del servidor
        JSONObject resEstablecimientoConexion = Utils.parsearString2Json(obtenerRespuestaServer(bufferedReader));

        System.out.println(resEstablecimientoConexion.get("msg"));

        long codigo = (long) resEstablecimientoConexion.get("codigo");
        if (codigo == 200){

            JSONArray partidas = (JSONArray) resEstablecimientoConexion.get("partidas");
            System.out.println("Hay " + partidas.size() + " partidas disponibles");
            for (Object partida : partidas){

                JSONObject partidaJson = (JSONObject) partida;
                System.out.println("Id partida -> \'" + partidaJson.get("id") + "\'. Tiene \'" + partidaJson.get("numJugadores") + "\' jugadores esperando.");
            }
        }
    }

    public void unirnosAPartida(String token){

        Scanner inputTerminal = new Scanner(System.in);
        System.out.println("¿Cuál es el id de la partida a la que quieres unirte?");
        String idPartida = inputTerminal.nextLine();

        Socket conexion = crearClientSocket();
        PrintWriter printWriter = obtenerPrintWriterSocket(conexion);
        BufferedReader bufferedReader = obtenerBufferedReaderSocket(conexion);

        JSONObject args = new JSONObject();
        args.put("token", token);
        args.put("idPartida", idPartida);

        JSONObject datosPeticion = new JSONObject();
        datosPeticion.put("metodo", Metodo.POST.name());
        datosPeticion.put("accion", "unirsePartidaMultijugador");
        datosPeticion.put("args", args);

        // Enviamos los datos de la petición
        printWriter.println(datosPeticion.toJSONString());

        // Recibimos la respuesta del servidor
        JSONObject resEstablecimientoConexion = Utils.parsearString2Json(obtenerRespuestaServer(bufferedReader));
        System.out.println(resEstablecimientoConexion.get("msg"));

        // Esperamos a que nos avisen de que la partida va a comenzar
        JSONObject resComienzoPartida = Utils.parsearString2Json(obtenerRespuestaServer(bufferedReader));
        System.out.println(resComienzoPartida.get("msg"));

    }


    /* --- UDP --- */
    public static DatagramSocket crearDatagramSocket(){
        try {
            return new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InetAddress obtenerDireccionLocal(){
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean enviarDatagrama(DatagramSocket datagramSocket, DatagramPacket datagramPacket){

        try {
            datagramSocket.send(datagramPacket);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static DatagramPacket recibirDatagrama(DatagramSocket datagramSocket, DatagramPacket dpEnviado){

        try {
            datagramSocket.receive(dpEnviado);
            return dpEnviado;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject realizarPeticionUDP(InetAddress direccion, int puerto, JSONObject jsonPeticion){

        // Step 1: Creamos el socket por el que enviaremos la información
        DatagramSocket datagramSocket = crearDatagramSocket();

        byte buf[] = jsonPeticion.toJSONString().getBytes();

        // Step 2 : Creamos el paquete del datagrama
        DatagramPacket dpEnviado = new DatagramPacket(buf, buf.length, direccion, puerto);

        // Step 3: Enviamos el datagrama al servidor
        boolean enviado = enviarDatagrama(datagramSocket, dpEnviado);

        // Comprobamos que se halla podido enviar la información
        if(enviado){

            // Limpiamos el buffer de bytes
            dpEnviado.setData(new byte[65536]);
            DatagramPacket dpRecibido = recibirDatagrama(datagramSocket, dpEnviado);

            // COmprobamos que hallamos recibido respuesta
            if (dpRecibido != null){

                JSONObject jsonRes = Utils.parsearBytes2Json(dpRecibido.getData());
                return jsonRes;
            }
        }
        return null;
    }
    /* ------------ */



    /* --- TCP --- */
    public static Socket crearClientSocket(){

        Socket socket;
        try {
            socket = new Socket(Constantes.LOCALHOST, Constantes.PUERTO_TCP);
            return socket;

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PrintWriter obtenerPrintWriterSocket(Socket socket){
        try {
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            return printWriter;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BufferedReader obtenerBufferedReaderSocket(Socket socket){
        try {
            BufferedReader temp = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return temp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String obtenerRespuestaServer(BufferedReader bufferedReader){

        try {
            String datos = bufferedReader.readLine();
            return datos;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /* ----------- */

}
