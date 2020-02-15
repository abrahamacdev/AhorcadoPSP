package ahorcado.server.modelo;

import ahorcado.server.utils.Metodo;
import ahorcado.server.utils.Protocolo;
import org.json.simple.parser.*;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Set;

public class Peticion {

    private Metodo metodo = null;
    private Protocolo protocolo = null;
    private String accion = "";
    private HashMap<String, String> argumentos = new HashMap<>();
    private JSONObject respuesta = null;

    // En el caso de TCP->Socket, UDP->DatagramPacket
    private Object peticionCliente = null;

    // Sólo estará disponible para UDP
    private DatagramSocket buzon = null;


    public Peticion(Protocolo protocolo, Object peticionCliente, String datos){
        this(protocolo, peticionCliente, null, datos);
    }

    public Peticion(Protocolo protocolo, Object peticionCliente, DatagramSocket buzon, String datos){
        this.protocolo = protocolo;
        this.peticionCliente = peticionCliente;

        // Si el protocolo es UDP, creamos un buzón para no solapar el que utiliza el hilo principal
        if (protocolo == Protocolo.UDP){
            crearBuzonUDP(buzon);
        }

        // Parseamos los datos de la petición
        parsearDatosJSON(datos);
    }

    private void crearBuzonUDP(DatagramSocket buzon){
        synchronized (buzon){
            try {
                this.buzon = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }

    private void parsearDatosJSON(String datos){

        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(datos);

            // Método de la petición
            Metodo tempMetodo = convertirStrToMetodo((String) jsonObject.get("metodo"));
            if (tempMetodo != null){
                metodo = tempMetodo;
            }

            // Acción a realizar
            accion = (String) jsonObject.get("accion");

            // Argumentos de la petición
            JSONObject args = (JSONObject) jsonObject.get("args");

            // Si la petición tiene argumentos, los obtendremos todos
            if (args != null){

                for (String key : ((Set<String>) args.keySet())){
                    argumentos.put(key, (String) args.get(key));
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private Metodo convertirStrToMetodo(String metodoPeticion){

        for (Metodo metodo : Metodo.values()){
            String tempMetodo = metodo.name().toUpperCase();
            if (tempMetodo.equals(metodoPeticion)){
                return metodo;
            }
        }
        return null;
    }

    public void enviarMensaje(JSONObject mensaje){

        // Respuesta a enviar en bytes
        String mens = mensaje.toJSONString();

        // Enviamos la respuesta a través del socket
        if (protocolo == Protocolo.TCP){
            Socket socket = (Socket) peticionCliente;

            PrintWriter printWriter = null;
            try {
                printWriter = new PrintWriter(socket.getOutputStream(),true);
                printWriter.println(mens);

            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (printWriter != null){
                    printWriter.close();
                }
            }
        }

        // Enviamos el datagrampacket de vuelta por el buzon
        else {
            byte[] res = mens.getBytes();
            DatagramPacket datagramPacket = (DatagramPacket) peticionCliente;
            datagramPacket.setData(res);
            try {
                buzon.send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void finalizar(){

        // Si no hay respuesta, creamos una por defecto
        if (respuesta == null){
            respuesta = new JSONObject();
            respuesta.put("codigo","500");
            respuesta.put("msg", "Algo salio mal");
        }

        // Respuesta a enviar en bytes
        String res = respuesta.toJSONString();

        // Enviamos la respuesta a través del socket
        if (protocolo == Protocolo.TCP){
            Socket socket = (Socket) peticionCliente;

            PrintWriter printWriter = null;
            try {
                printWriter = new PrintWriter(socket.getOutputStream(),true);
                printWriter.println(res);

            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (printWriter != null){
                    printWriter.close();
                }

                if (socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Enviamos el datagrampacket de vuelta por el buzon
        else {
            DatagramPacket datagramPacket = (DatagramPacket) peticionCliente;
            byte[] resBytes = res.getBytes();
            datagramPacket.setData(resBytes);
            try {
                buzon.send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            buzon.close();
        }
    }

    @Override
    public String toString() {
        return "(Peticion) Método: " + metodo + "\n" +
                        "Accion: " + accion + "\n" +
                        "Args: " + argumentos;
    }

    public Metodo getMetodo() {
        return metodo;
    }

    public void setMetodo(Metodo metodo) {
        this.metodo = metodo;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public HashMap<String, String> getArgumentos() {
        return argumentos;
    }

    public void setArgumentos(HashMap<String, String> argumentos) {
        this.argumentos = argumentos;
    }

    public Object getPeticionCliente() {
        return peticionCliente;
    }

    public void setPeticionCliente(Object peticionCliente) {
        this.peticionCliente = peticionCliente;
    }

    public Protocolo getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(Protocolo protocolo) {
        this.protocolo = protocolo;
    }

    public DatagramSocket getBuzon() {
        return buzon;
    }

    public void setBuzon(DatagramSocket buzon) {
        this.buzon = buzon;
    }

    public JSONObject getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(JSONObject respuesta) {
        this.respuesta = respuesta;
    }
}
