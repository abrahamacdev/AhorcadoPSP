package ahorcado.server.vista;

import ahorcado.server.controlador.escuchadores.EscuchadorTCP;
import ahorcado.server.controlador.escuchadores.EscuchadorUDP;

public class ServerMain {

    public static void levantarServidor(){

        // Comenzamos a escuchar peticiones
        new EscuchadorTCP().start();
        new EscuchadorUDP().start();
    }
}
