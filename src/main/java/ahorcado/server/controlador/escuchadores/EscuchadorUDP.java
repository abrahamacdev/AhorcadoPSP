package ahorcado.server.controlador.escuchadores;

import ahorcado.server.controlador.Enrutador;
import ahorcado.server.controlador.manejadores.IManejador;
import ahorcado.server.utils.Constantes;
import ahorcado.server.utils.Protocolo;
import ahorcado.server.modelo.Peticion;
import ahorcado.server.utils.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class EscuchadorUDP extends Thread {

    private DatagramSocket buzon;

    @Override
    public void run() {

        // Creamos el socket por el que escucharemos las peticiones
        crearBuzon();

        while (true){

            // Esperamos a que nos llegue una petición
            DatagramPacket paquete = esperarPeticion();
            manejarPeticionEntrante(paquete);
        }
    }

    private void crearBuzon(){
        try {
            buzon = new DatagramSocket(Constantes.PUERTO_UDP);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private DatagramPacket esperarPeticion(){

        byte[] recibidos = new byte[65535];
        DatagramPacket dgEntrante = new DatagramPacket(recibidos, recibidos.length);

        try {
            buzon.receive(dgEntrante);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dgEntrante;
    }

    private void manejarPeticionEntrante(DatagramPacket paqueteEntrante){

        // Obtenemos los datos de la petición
        String data = Utils.byteToString(paqueteEntrante.getData());

        // Creamos la petición de tipo UDP
        Peticion peticion = new Peticion(Protocolo.UDP, paqueteEntrante, buzon, data);

        // Obtenemos el manejador de la petición y dejamos que el se encargue
        // de realizar las acciones necesarias
        IManejador iManejador = Enrutador.instance.enrutar(peticion);
        if (iManejador != null){

            iManejador.manejarConexionEntrante(peticion);
        }
    }
}
