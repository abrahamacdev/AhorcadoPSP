package ahorcado.server.modelo;

import ahorcado.server.controlador.PartidaChangeListener;
import ahorcado.server.controlador.protocolos.ProtocoloPartida;
import ahorcado.server.utils.Par;
import ahorcado.server.utils.Utils;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Partida {

    private int id;
    private PartidaChangeListener partidaChangeListener;
    private ArrayList<ProtocoloPartida> protocolosJugadores = new ArrayList<>();
    private String palabraCompleta;
    private HashMap<Usuario,String> palabraIncompletaDeCadaUsuario = new HashMap<>();
    private HashMap<Usuario, Integer> letrasAcertadasPorIdJugador = new HashMap<>();
    private boolean comenzada = false;

    private HashMap<Usuario, Peticion> jugadorYSuPeticion = new HashMap<>();
    private HashMap<Usuario, PrintWriter> jugadorYSuWriter = new HashMap<>();
    private HashMap<Usuario, BufferedReader> jugadorYSuReader = new HashMap<>();

    private static int ultId = 0;
    private synchronized static int crearId(){
        int temp = ultId;
        ultId++;
        return temp;
    }


    public Partida(Peticion peticion, Usuario usuario, PartidaChangeListener partidaChangeListener){
        this.id = crearId();
        this.palabraCompleta = Utils.obtenerPalabraRandom();
        this.partidaChangeListener = partidaChangeListener;

        // Añadimos al usuario creado de la partida a la lista de jugadores
        anadirJugador(usuario,peticion);

        // Indicamos que estamos a la espera de jugadores
        partidaChangeListener.onPartidaEsperando(this);
    }



    public void comenzar(){

        if (!comenzada){

            this.comenzada = true;
            inicializarPalabrasIncompletas();
            inicializarLetrasAcertadasPorJug();
            crearProtocoloJugadores();
            avisarComienzoPartidaJugadores();

            System.out.println(palabraIncompletaDeCadaUsuario);
            System.out.println(" ----------------- ");
            System.out.println(letrasAcertadasPorIdJugador);

            // Indicamos que estamos jugando la partida
            partidaChangeListener.onPartidaComenzada(this);
        }
    }

    public boolean anadirJugador(Usuario usuario, Peticion peticion){

        // Añadimos a un jugador si no está ya en la lista y la partida aún no ha comenzado
        if (!jugadorYSuPeticion.containsKey(usuario) && !comenzada){

            Socket socket = (Socket) peticion.getPeticionCliente();
            jugadorYSuPeticion.put(usuario,peticion);
            jugadorYSuWriter.put(usuario, Utils.obtenerWriterDeSocket(socket));
            jugadorYSuReader.put(usuario, Utils.obtenerReaderDeSocket(socket));

            // TODO Eliminar si queremos esperar a que se unan más usuarios
            if (jugadorYSuPeticion.size() == 2){
                comenzar();
            }

            return true;
        }
        return false;
    }

    public boolean estaEnPartida(Usuario usuario){
        return jugadorYSuPeticion.containsKey(usuario);
    }




    private void inicializarPalabrasIncompletas(){
        for (Usuario usuario : jugadorYSuPeticion.keySet()){
            if (!palabraIncompletaDeCadaUsuario.containsKey(usuario)){
                palabraIncompletaDeCadaUsuario.put(usuario, Utils.obtenerPalabraIncompleta(palabraCompleta));
            }
        }
    }

    private void crearProtocoloJugadores(){

        String incompleta = palabraIncompletaDeCadaUsuario.get(jugadorYSuPeticion.keySet().iterator().next().getId());

        // Creamos un protocolo para cada usuario
        for (Usuario usuario : jugadorYSuPeticion.keySet()){
            protocolosJugadores.add(new ProtocoloPartida(palabraCompleta, incompleta));
        }
    }

    private void avisarComienzoPartidaJugadores(){

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("codigo",200);
        jsonObject.put("msg","La partida va a comenzar...");

        // Avisamos a todos los jugadores de que la partida va a comenzar
        // Esto se hará de forma síncrona desde el mismo hilo
        for (Usuario usuario : jugadorYSuWriter.keySet()){

            PrintWriter printWriter = jugadorYSuWriter.get(usuario);
            printWriter.println(jsonObject.toJSONString());
        }
    }

    private void inicializarLetrasAcertadasPorJug() {

        for (Usuario usuario : jugadorYSuPeticion.keySet()){
            letrasAcertadasPorIdJugador.put(usuario,0);
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
        return jugadorYSuPeticion.size();
    }
}
