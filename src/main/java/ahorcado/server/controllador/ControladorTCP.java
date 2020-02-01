package ahorcado.server.controllador;

import java.util.Timer;
import java.util.TimerTask;

public class ControladorTCP extends Thread{

    @Override
    public void run() {
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
