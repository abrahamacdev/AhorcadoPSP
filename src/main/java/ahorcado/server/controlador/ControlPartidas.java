package ahorcado.server.controlador;

import ahorcado.server.modelo.Partida;
import ahorcado.server.modelo.Peticion;
import ahorcado.server.modelo.Usuario;
import ahorcado.server.utils.Par;

import java.util.ArrayList;
import java.util.Optional;

public class ControlPartidas implements PartidaChangeListener {

    private final ArrayList<Partida> partidasCreadas = new ArrayList<>();
    private final ArrayList<Partida> partidasComenzadas = new ArrayList<>();
    private final ArrayList<Partida> partidasEsperando = new ArrayList<>();
    private final ArrayList<Partida> partidasAcabadas = new ArrayList<>();

    private static ControlPartidas controlPartidas;

    private ControlPartidas(){}

    public static ControlPartidas getInstance(){

        if (controlPartidas == null){
            controlPartidas = new ControlPartidas();
        }
        return controlPartidas;
    }

    public synchronized boolean estaEnPartida(Usuario usuario){

        for (Partida partida : partidasCreadas){
            if (partida.estaEnPartida(usuario)){
                return true;
            }
        }
        return false;
    }

    public synchronized Partida crearPartida(Peticion peticion, Usuario usuario){

        // Creamos la partida si el usuario aún no está en ninguna
        if (!estaEnPartida(usuario)){
            Partida partida = new Partida(peticion,usuario, this);
            partidasCreadas.add(partida);
            return partida;
        }

        return null;
    }

    public synchronized Partida obtenerPartidaPorId(int id){
        Optional<Partida> optPartida = partidasEsperando.stream()
                .filter((partida) -> partida.getId() == id)
                .findFirst();

        if (optPartida.isPresent()){
            return optPartida.get();
        }
        return null;
    }


    public void onPartidaEsperando(Partida partida) {
        synchronized (partidasEsperando){
            partidasEsperando.add(partida);
        }
    }

    @Override
    public void onPartidaComenzada(Partida partida) {
        synchronized (partidasComenzadas){
            partidasComenzadas.add(partida);
            synchronized (partidasEsperando){
                partidasEsperando.remove(partida);
            }
        }
    }

    @Override
    public void onPartidaAcabada(Partida partida) {
        synchronized (partidasComenzadas){
            partidasComenzadas.remove(partida);
            synchronized (partidasAcabadas){
                partidasAcabadas.remove(partida);
            }
        }
    }


    public synchronized ArrayList<Partida> getPartidasCreadas() {
        return partidasCreadas;
    }

    public synchronized ArrayList<Partida> getPartidasEsperando(){
        return partidasEsperando;
    }
}
