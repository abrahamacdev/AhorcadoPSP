package ahorcado.server.controlador.manejadores;

import ahorcado.server.modelo.Peticion;

public interface IManejador {

    public boolean contieneRecurso(Peticion peticion);

    public void manejarConexionEntrante(Peticion peticion);
}
