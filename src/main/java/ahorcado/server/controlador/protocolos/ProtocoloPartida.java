package ahorcado.server.controlador.protocolos;

import ahorcado.server.utils.Constantes;
import ahorcado.server.utils.Par;
import ahorcado.server.utils.Utils;
import java.util.HashMap;

public class ProtocoloPartida {

    // Estado de la partida en el que nos encontramos
    private int estadoActual = Constantes.ProtocoloPartida.INICIAL;
    private int estadoMuneco = Constantes.ProtocoloPartida.SIN_FALLO;
    private int vidasRestantes = Constantes.ProtocoloPartida.NUMERO_VIDAS;
    private String palabraCompleta = Utils.obtenerPalabraRandom();
    private String palabraIncompleta = Utils.obtenerPalabraIncompleta(palabraCompleta);

    public ProtocoloPartida(){}

    public ProtocoloPartida(String palabraCompleta, String palabraIncompleta){
        this.palabraCompleta = palabraCompleta;
        this.palabraIncompleta = palabraIncompleta;
    }

    public HashMap<String, Object> jugar(String palabraJugador){

        boolean acertado = true;

        HashMap<String, Object> respuesta = new HashMap<>();
        respuesta.put("palabraCompleta", palabraCompleta);
        respuesta.put("palabraIncompleta", palabraIncompleta);
        respuesta.put("acertado", acertado);

        // Nos encontramos en el comienzo de la partida
        if (estadoActual == Constantes.ProtocoloPartida.INICIAL){
            estadoActual = Constantes.ProtocoloPartida.SIN_FALLO;
            respuesta.put("estado", estadoActual);
            respuesta.put("resumen", obtenerDibujo());
            respuesta.put("acertado", false);
            respuesta.put("vidas", vidasRestantes);
            return respuesta;
        }

        // Mientras queden vidas...
        if (vidasRestantes >= 0){

            char letra = (char) 1;
            String palabra = null;

            // Nos quedamos con la palabra o con la letra (según lo que mande)
            if (palabraJugador.length() == 1){ letra = palabraJugador.charAt(0);}
            else { palabra = palabraJugador; }

            // Ha escrito una letra
            if (letra != 1){
                // Ha dicho una letra que no se encuentra en la palabra completa
                if(!Utils.stringContieneLetra(letra, palabraCompleta)){
                    acertado = false;
                }

                // La letra se encuentra en la palabra completa
                else {
                    // Añadimos la letra a la palabra incompleta
                    if(!Utils.stringContieneLetra(letra, palabraIncompleta)){
                        palabraIncompleta = Utils.anadirLetraAPalabraIncompleta(letra, palabraIncompleta, palabraCompleta);
                    }

                    // La letra ya se había añadido antes
                    else {
                        acertado = false;
                    }
                }
            }

            // Ha escrito una palabra
            else if (palabra != null){

                // No ha acertado la palabra
                if(!palabraCompleta.equals(palabra)) {
                    acertado = false;
                }

                // Ha ganado el juego
                else {
                    estadoActual = Constantes.ProtocoloPartida.FIN;
                    respuesta.put("acertado", acertado);
                    respuesta.put("estado", estadoActual);
                    respuesta.put("resumen", obtenerDibujo());
                    respuesta.put("vidas", vidasRestantes);
                    respuesta.put("palabraIncompleta", palabraIncompleta);
                    return respuesta;
                }
            }

            // Ha completado la palabra a base de añadir letras individuales
            if (palabraCompleta.equals(palabraIncompleta)){
                estadoActual = Constantes.ProtocoloPartida.FIN;
                respuesta.put("acertado", acertado);
                respuesta.put("estado", estadoActual);
                respuesta.put("resumen", obtenerDibujo());
                respuesta.put("vidas", vidasRestantes);
                respuesta.put("palabraIncompleta", palabraIncompleta);
                return respuesta;
            }

            // Restamos una vida
            if (!acertado){
                vidasRestantes--;
                estadoMuneco++;
            }

            // Enviamos el resumen actual de la partida
            if (vidasRestantes >= 0){
                respuesta.put("acertado", acertado);
                respuesta.put("estado", estadoActual);
                respuesta.put("resumen", obtenerDibujo());
                respuesta.put("vidas", vidasRestantes);
                respuesta.put("palabraIncompleta", palabraIncompleta);
                return respuesta;
            }
        }

        // Ya no quedan vidas
        if (vidasRestantes < 0){

            estadoActual = Constantes.ProtocoloPartida.FIN;

            respuesta.put("acertado", false);
            respuesta.put("estado", estadoActual);
            respuesta.put("resumen", obtenerDibujo());
            respuesta.put("vidas", vidasRestantes);
            return respuesta;
        }
        return null;
    }

    private String obtenerDibujo(){

        String dibujo = Constantes.ProtocoloPartida.PARTE_SUPERIOR + "\n";

        int filas = Constantes.ProtocoloPartida.NUMERO_POSTES;
        int columnas = (int) Constantes.ProtocoloPartida.POSICION_MIEMBRO_BRAZO_DER.segundo + 1;
        char relleno = ' ';

        for (int i=0; i<filas; i++){

            // Creamos el texto de la fila actual y lo rellenamos con espacios
            String textoFila = Constantes.ProtocoloPartida.POSTE;
            for (int f=1; f<columnas; f++){
                textoFila += ' ';
            }

            for (int j=0; j<columnas; j++){
                if (estadoMuneco >= Constantes.ProtocoloPartida.CABEZA){
                    for (int c=Constantes.ProtocoloPartida.CABEZA; c <= estadoMuneco; c++){

                        // Obtenemos las posiciones del dibujo y el texto a escribir
                        Par par = Constantes.ProtocoloPartida.MOMENTO_DIBUJO.get(c);

                        String letra = (String) par.primero;
                        Par posiciones = (Par) par.segundo;

                        int fila = (int) posiciones.primero;
                        int columna = (int) posiciones.segundo;

                        // Comprobamos que la fila y la columna coincidan con la posición actual
                        if ((int) posiciones.primero == i && (int) posiciones.segundo == j){
                            textoFila = anadirLetraEnPosicion(textoFila, letra.charAt(0), columna);
                        }
                    }
                }
            }

            // Añadimos un salto de línea
            dibujo += textoFila + "\n";
        }

        dibujo += Constantes.ProtocoloPartida.PARTE_INFERIOR;

        return dibujo;
    }

    private String anadirLetraEnPosicion(String cadena, char letra, int posicion){

        char[] cadenaModificada = cadena.toCharArray();
        cadenaModificada[posicion] = letra;
        return new String(cadenaModificada);
    }
}
