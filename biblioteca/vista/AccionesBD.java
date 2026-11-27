package biblioteca.vista;

import biblioteca.persistencia.BaseDatos;
import biblioteca.servicio.Biblioteca;

import java.awt.Component;

/**
 * Acciones del menú Archivo para la base de datos SQLite. Si falta el driver o hay un error,
 * se muestra un aviso y el programa sigue funcionando con los datos que ya tenía.
 */
class AccionesBD {
    static final String ARCHIVO_BD = "datos/biblioteca.db";

    private AccionesBD() { }

    static void guardar(Sesion sesion, Component padre) {
        try {
            new BaseDatos(ARCHIVO_BD).guardar(sesion.biblioteca);
            Mensajes.info(padre, "Datos guardados en la base de datos " + ARCHIVO_BD);
        } catch (Exception ex) {
            Mensajes.aviso(padre, "No se pudo usar la base de datos: " + ex.getMessage());
        }
    }

    static void cargar(Sesion sesion, Component padre) {
        if (!Mensajes.confirmar(padre, "Se reemplazarán los datos actuales por los de la base de datos. ¿Continuar?")) return;
        try {
            Biblioteca cargada = new BaseDatos(ARCHIVO_BD).cargar();
            sesion.biblioteca = cargada;
            sesion.refrescar.run();
            Mensajes.info(padre, "Datos cargados desde la base de datos " + ARCHIVO_BD);
        } catch (Exception ex) {
            Mensajes.aviso(padre, "No se pudo usar la base de datos: " + ex.getMessage());
        }
    }
}
