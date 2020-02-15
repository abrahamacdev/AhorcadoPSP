package ahorcado.server.utils;

import com.sun.security.ntlm.Server;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;

public class Utils {

    private static Session session = null;
    private static Random random = new Random();

    public final static String byteToString(byte[] bytes){
        if (bytes == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (bytes[i] != 0)
        {
            ret.append((char) bytes[i]);
            i++;
        }
        return ret.toString();
    }

    public static Session obtenerSession() {
        if (session == null){
            session = new Configuration().configure().buildSessionFactory().openSession();
        }
        return session;
    }

    public static Session obtenerSessionPersonalizada(String rutaDelRecursoXML){
        if (session == null){
            session = new Configuration().configure(rutaDelRecursoXML).buildSessionFactory().openSession();
        }
        return session;
    }

    public static UUID generarUUID(){
        return UUID.randomUUID();
    }

    public static JSONObject parsearBytes2Json(byte[] res){

        String rawRes = Utils.byteToString(res);

        try {
            return (JSONObject) new JSONParser().parse(rawRes);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject parsearString2Json(String res){

        try {
            return (JSONObject) new JSONParser().parse(res);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String obtenerPalabraRandom(){
        int max = Constantes.ProtocoloPartida.POSIBLES_PALABRAS.length;
        return Constantes.ProtocoloPartida.POSIBLES_PALABRAS[random.nextInt(max)];
    }

    public static String obtenerPalabraIncompleta(String palabra){

        String palabraIncompleta = new String(palabra);

        // Obtenemos una lista con los distintos caracteres de la palabra
        ArrayList<Character> distintosCaracteres = new ArrayList<>();
        for (int i=0; i<palabra.toCharArray().length; i++){
            char temp = palabra.charAt(i);

            if (!distintosCaracteres.contains(temp)){
                distintosCaracteres.add(temp);
            }
        }

        // La primera mitad de la lista la usaremos como caracteres ocultos
        ArrayList<Character> caracteresAOcultar = new ArrayList<>();
        int mitad = distintosCaracteres.size()/ 2;
        for (int i = 0; i<mitad; i++){
            char temp = distintosCaracteres.get(i);
            caracteresAOcultar.add(temp);
        }

        // Reemplazamos los caracteres a ocultar por '_' en la palabta incompleta
        for (Character caracter : caracteresAOcultar){
            palabraIncompleta = palabraIncompleta.replaceAll(String.valueOf(caracter), "_");
        }

        return palabraIncompleta;
    }

    public static String anadirLetraAPalabraIncompleta(char letra, String palabraIncompleta, String palabraCompleta){

        // Nos quedamos con las posiciones en las que tendremos que colocar la letra
        ArrayList<Integer> posiciones = new ArrayList<>();
        int i = 0;
        for (char tempLetra : palabraCompleta.toCharArray()){
            if (tempLetra == letra){
                posiciones.add(i);
            }
            i++;
        }

        // Cambiamos el caracter de la posición "x" por la letra correspondiente
        char[] charArrayNuevaPalInc = palabraIncompleta.toCharArray();
        for (int posicion : posiciones){
            charArrayNuevaPalInc[posicion] = letra;
        }

        // Creamos una nueva cadena con los caracteres modificados
        return new String(charArrayNuevaPalInc);
    }

    public static boolean stringContieneLetra(char letra, String cadena){

        for (char chaar : cadena.toCharArray()){
            if (letra == chaar){
                return true;
            }
        }

        return false;
    }
}
