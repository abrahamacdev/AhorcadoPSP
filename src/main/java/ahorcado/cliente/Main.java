package ahorcado.cliente;

import ahorcado.server.utils.Metodo;
import ahorcado.server.utils.Utils;
import ahorcado.server.vista.ServerMain;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.*;

public class Main {

    public static void main(String[] args){

        Main main = new Main();

        // Levantamos el servidor
        ServerMain.levantarServidor();

        // Registramos un nuevo usuario
        main.realizarRegistroCliente();
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

        JSONObject jsonRes = realizarPeticionUDP(inetAddress, Utils.PUERTO_UDP, jsonPeticion);

        if (jsonRes != null){
            System.out.println("Respuesta del servidor a nuestra petición de registrarnos: " + jsonRes.toJSONString());
        }

    }

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

    public static JSONObject parsearRespuesta(byte[] res){

        String rawRes = Utils.byteToString(res);

        try {
            return (JSONObject) new JSONParser().parse(rawRes);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject realizarPeticionUDP(InetAddress direccion, int puerto, JSONObject jsonPeticion){

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

                JSONObject jsonRes = parsearRespuesta(dpRecibido.getData());
                return jsonRes;
            }
        }
        return null;
    }
}
