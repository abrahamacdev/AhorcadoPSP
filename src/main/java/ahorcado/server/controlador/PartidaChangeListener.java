package ahorcado.server.controlador;

import ahorcado.server.modelo.Partida;
import ahorcado.server.utils.Par;

public interface PartidaChangeListener {

    void onPartidaEsperando(Partida partida);

    void onPartidaComenzada(Partida partida);

    void onPartidaAcabada(Partida partida);

}
