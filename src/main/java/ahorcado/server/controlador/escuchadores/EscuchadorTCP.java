package ahorcado.server.controlador.escuchadores;

import ahorcado.server.controlador.Enrutador;
import ahorcado.server.controlador.manejadores.IManejador;
import ahorcado.server.modelo.Peticion;
import ahorcado.server.utils.Constantes;
import ahorcado.server.utils.Protocolo;

import java.io.*;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class EscuchadorTCP extends Thread{

    private ServerSocket serverSocket;

    @Override
    public void run() {

        crearServerSocket();

        while (true){

            Socket peticionEntrante = esperarPeticion();
            manejarPeticionEntrante(peticionEntrante);
        }
    }

    private void crearServerSocket(){

        try {
            serverSocket = new ServerSocket(Constantes.PUERTO_TCP);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Socket esperarPeticion(){

        try {
            return serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void manejarPeticionEntrante(Socket peticionEntrante){

        // Obtenemos los datos de la petición
        String data = leerDatosPeticionEntrante(peticionEntrante);

        // Creamos la petición de tipo TCP
        Peticion peticion = new Peticion(Protocolo.TCP, peticionEntrante, data);

        // Obtenemos el manejador de la petición y dejamos que el se encargue
        // de realizar las acciones necesarias
        IManejador iManejador = Enrutador.instance.enrutar(peticion);
        if (iManejador != null){
            iManejador.manejarConexionEntrante(peticion);
        }
    }

    private String leerDatosPeticionEntrante(Socket peticionEntrante){

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(peticionEntrante.getInputStream()));

            // Leémos sólo la primera línea (la que contendrá el json con los datos de la petición)
            String datos = bufferedReader.readLine();

            if (datos != null){
                return datos;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void mostrarTCPNoDisponible(){

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("El hilo de peticiones TCP no está escuchando ninguna petición...");
            }
        };

        long tiempoEntreTicksEnSeg = 3;

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 1000, tiempoEntreTicksEnSeg * 1000);
    }
}
