package ahorcado.server.utils;

import java.util.TreeMap;

public class Constantes {

    public final static int PUERTO_UDP = 25560;
    public final static int PUERTO_TCP = 25561;
    public final static String LOCALHOST  = "127.0.0.1";

    public static class ProtocoloPartida {

        public static int NUMERO_VIDAS = 5;
        public static String[] POSIBLES_PALABRAS = new String[]{
                "platano",
                "melocoton",
                "albaricoque",
                "manzana",
                "frambuesa"
        };

        public static int INICIAL = 0;
        public static int SIN_FALLO = 1;
        public static int CABEZA = 2;
        public static int BRAZO_IZQ = 3;
        public static int TRONCO = 4;
        public static int BRAZO_DER = 5;
        public static int PIERNA_IZQ = 6;
        public static int PIERNA_DER = 7;
        public static int FIN = 8;

        public static String PARTE_SUPERIOR = "-------|";
        public static String POSTE = "|";
        public static String PARTE_INFERIOR = "=============";
        public static int NUMERO_POSTES = 4;

        // Posición de la cabeza
        public static String MIEMBRO_CABEZA = "O";
        public static Par POSICION_MIEMBRO_CABEZA = new Par(0,7);

        // Posición del tronco
        public static String MIEMBRO_TRONCO = "|";
        public static Par POSICION_MIEMBRO_TRONCO = new Par(1,7);

        // Posición del brazo derecho
        public static String MIEMBRO_BRAZO_DER = "\\";
        public static Par POSICION_MIEMBRO_BRAZO_DER = new Par(1,8);

        // Posición del brazo izquierdo
        public static String MIEMBRO_BRAZO_IZQ = "/";
        public static Par POSICION_MIEMBRO_BRAZO_IZQ = new Par(1,6);

        // Posición de la pierna derecha
        public static String MIEMBRO_PIERNA_DER = "\\";
        public static Par POSICION_MIEMBRO_PIERNA_DER = new Par(2,8);

        // Posición de la pierna izquierda
        public static String MIEMBRO_PIERNA_IZQ = "/";
        public static Par POSICION_MIEMBRO_PIERNA_IZQ = new Par(2,6);

        // Asociación entre situación del juego y miembros a pintar
        public static TreeMap<Integer, Par> MOMENTO_DIBUJO = new TreeMap<Integer, Par>(){{
            put(CABEZA, new Par(MIEMBRO_CABEZA, POSICION_MIEMBRO_CABEZA));
            put(TRONCO, new Par(MIEMBRO_TRONCO, POSICION_MIEMBRO_TRONCO));
            put(BRAZO_DER, new Par(MIEMBRO_BRAZO_DER, POSICION_MIEMBRO_BRAZO_DER));
            put(BRAZO_IZQ, new Par(MIEMBRO_BRAZO_IZQ, POSICION_MIEMBRO_BRAZO_IZQ));
            put(PIERNA_DER, new Par(MIEMBRO_PIERNA_DER, POSICION_MIEMBRO_PIERNA_DER));
            put(PIERNA_IZQ, new Par(MIEMBRO_PIERNA_IZQ, POSICION_MIEMBRO_PIERNA_IZQ));
        }};
    }
}
