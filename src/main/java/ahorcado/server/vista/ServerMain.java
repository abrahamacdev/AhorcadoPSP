package ahorcado.server.vista;

import ahorcado.server.controllador.ControladorTCP;
import ahorcado.server.controllador.ControladorUDP;

public class ServerMain {

    public static void levantarServidor(){

        // Comenzamos a escuchar peticiones
        new ControladorTCP().start();
        new ControladorUDP().start();
    }
}
