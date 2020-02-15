package ahorcado.cliente;

import ahorcado.server.utils.Constantes;
import ahorcado.server.utils.Metodo;
import ahorcado.server.utils.Utils;
import ahorcado.server.vista.ServerMain;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class Main {

    public static void main(String[] args){

        Main main = new Main();

        // Levantamos el servidor
        ServerMain.levantarServidor();




        /* --- UDP --- */
        // Registramos un nuevo usuario
        //main.realizarRegistroCliente();

        // Nos logueamos/deslogueamos con el usuario existente
        String token = main.realizarLoginCliente();
        //main.realizarLogoutCliente();
        /* ----------- */




        /* --- TCP --- */
        // Comenzamos una nueva partida
        //main.comenzarNuevaPartida(token);
        /* ----------- */
    }

    public void realizarRegistroCliente(){

        InetAddress inetAddress = obtenerDireccionLocal();

        JSONObject jsonPeticion = new JSONObject();
        jsonPeticion.put("metodo",Metodo.POST.name());
        jsonPeticion.put("accion","registro");

        JSONObject jsonArgs = new JSONObject();
        jsonArgs.put("nombre","Abraham");
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
        jsonArgs.put("nombre","Abraham");
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
        jsonArgs.put("nombre","Abraham");

        jsonPeticion.put("args", jsonArgs);

        JSONObject jsonRes = realizarPeticionUDP(inetAddress, Constantes.PUERTO_UDP, jsonPeticion);

        if (jsonRes != null){
            System.out.println("Respuesta del servidor a nuestra petición de loguearnos: " + jsonRes.toJSONString());
        }
    }

    public void comenzarNuevaPartida(String token){

        Socket conexion = crearClientSocket();
        PrintWriter printWriter = obtenerPrintWriterSocket(conexion);
        BufferedReader bufferedReader = obtenerBufferedReaderSocket(conexion);

        JSONObject args = new JSONObject();
        args.put("token", token);

        JSONObject datosPeticion = new JSONObject();
        datosPeticion.put("metodo", Metodo.POST.name());
        datosPeticion.put("accion", "nuevaPartida");
        datosPeticion.put("args", args);

        // Enviamos los datos de la petición
        printWriter.println(datosPeticion.toJSONString());

        // Recibimos la respuesta del servidor
        JSONObject resEstablecimientoConexion = Utils.parsearString2Json(obtenerResEstablecimientoCon(bufferedReader));

        System.out.println(resEstablecimientoConexion.get("msg"));

        long codigo = (long) resEstablecimientoConexion.get("codigo");
        if (codigo == 200){

            BufferedReader readerJugador = new BufferedReader(new InputStreamReader(System.in));
            try {

                boolean partidaFinalizada = false;
                while (!partidaFinalizada) {

                    // Leemos la palabra de entrada
                    System.out.println("Introduzca una letra o palabra");
                    String entrada = readerJugador.readLine();

                    // Creamos un json y se la enviamos al servidor
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("palabra", entrada);
                    printWriter.println(jsonObject.toJSONString());

                    // Obtenemos el resultado de la palabra enviada
                    JSONObject resPalabra = Utils.parsearString2Json(obtenerResEstablecimientoCon(bufferedReader));
                    String msg = (String) resPalabra.get("msg");
                    partidaFinalizada = (boolean) resPalabra.get("finalizada");

                    System.out.println(msg);
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
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

    public static String obtenerResEstablecimientoCon(BufferedReader bufferedReader){

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
