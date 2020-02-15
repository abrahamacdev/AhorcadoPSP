package ahorcado.server.modelo;

import ahorcado.server.controlador.PartidaChangeListener;
import ahorcado.server.controlador.protocolos.ProtocoloPartida;
import ahorcado.server.utils.Par;
import ahorcado.server.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Partida {

    private int id;
    private Peticion peticionUsuarioCreador;
    private PartidaChangeListener partidaChangeListener;
    private ArrayList<Usuario> jugadores;
    private ArrayList<ProtocoloPartida> protocolosJugadores;
    private String palabraCompleta;
    private HashMap<Integer,String> palabraIncompletaDeCadaUsuario;
    private HashMap<Integer, Integer> letrasAcertadasPorIdJugador;
    private boolean comenzada = false;

    private static int ultId = 0;
    private synchronized static int crearId(){
        int temp = ultId;
        ultId++;
        return temp;
    }


    public Partida(Peticion peticion, Usuario usuario, PartidaChangeListener partidaChangeListener){
        this(peticion, new ArrayList<>(Arrays.asList(usuario)), partidaChangeListener);
    }

    public Partida(Peticion peticion, ArrayList<Usuario> jugadores, PartidaChangeListener partidaChangeListener){
        this.peticionUsuarioCreador = peticion;
        this.id = crearId();
        this.jugadores = jugadores;
        this.palabraCompleta = Utils.obtenerPalabraRandom();
        this.letrasAcertadasPorIdJugador = new HashMap<>();
        this.partidaChangeListener = partidaChangeListener;

        // Indicamos que estamos a la espera de jugadores
        partidaChangeListener.onPartidaEsperando(this);
    }



    public void comenzar(){
        this.comenzada = true;
        inicializarPalabrasIncompletas();
        crearProtocoloJugadores();

        // Indicamos que estamos jugando la partida
        partidaChangeListener.onPartidaComenzada(this);

    }

    public boolean anadirJugador(Usuario usuario, Peticion peticion){

        // Añadimos a un jugador si no está ya en la lista y la partida aún no ha comenzado
        if (!this.jugadores.contains(usuario) && !comenzada){
            jugadores.add(usuario);

            // TODO Eliminar si queremos esperar a que se unan más usuarios
            if (this.jugadores.size() == 2){
                comenzar();
            }

            return true;
        }
        return false;
    }

    public boolean estaEnPartida(Usuario usuario){
        return jugadores.contains(usuario);
    }




    private void inicializarPalabrasIncompletas(){
        for (Usuario usuario : jugadores){
            if (!palabraIncompletaDeCadaUsuario.containsKey(usuario.getId())){
                palabraIncompletaDeCadaUsuario.put(usuario.getId(), Utils.obtenerPalabraIncompleta(palabraCompleta));
            }
        }
    }

    private void crearProtocoloJugadores(){

        String incompleta = palabraIncompletaDeCadaUsuario.get(jugadores.get(0).getId());

        // Creamos un protocolo para cada usuario
        for (Usuario usuario : jugadores){
            protocolosJugadores.add(new ProtocoloPartida(palabraCompleta, incompleta));
        }
    }


    @Override
    public boolean equals(Object obj) {

        if (obj == null)return false;
        if (! (obj instanceof Partida)) return false;

        Partida partida = (Partida) obj;
        return partida.getId() == this.id;
    }

    public int getId() {
        return id;
    }

    public int getNumeroJugadores(){
        return jugadores.size();
    }
}
