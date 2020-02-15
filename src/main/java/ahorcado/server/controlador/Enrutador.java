package ahorcado.server.controlador;

import ahorcado.server.controlador.manejadores.PartidaManejador;
import ahorcado.server.controlador.manejadores.UsuariosManejador;
import ahorcado.server.controlador.manejadores.IManejador;
import ahorcado.server.modelo.Peticion;

import java.util.ArrayList;

public class Enrutador {

    public static Enrutador instance = new Enrutador();

    private final ArrayList<Class<? extends IManejador>> manejadores = new ArrayList<Class<? extends IManejador>>(){{
        add(UsuariosManejador.class);
        add(PartidaManejador.class);
    }};

    public synchronized IManejador enrutar(Peticion peticion){
        for (Class<? extends IManejador> manejador :  manejadores){
            try {
                IManejador temp = manejador.newInstance();
                if (temp.contieneRecurso(peticion)){
                    return temp;
                }

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
