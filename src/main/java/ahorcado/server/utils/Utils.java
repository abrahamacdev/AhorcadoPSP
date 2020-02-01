package ahorcado.server.utils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.nio.charset.StandardCharsets;

public class Utils {

    public final static int PUERTO_UDP = 25560;
    public final static int PUERTO_TCP = 25561;

    private static Session session = null;

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
}
