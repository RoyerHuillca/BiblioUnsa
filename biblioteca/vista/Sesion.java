package biblioteca.vista;

import biblioteca.servicio.Biblioteca;

import java.awt.Frame;

/** Estado compartido entre la ventana principal y sus paneles. */
public class Sesion {
    public Biblioteca biblioteca;
    public Frame ventana;
    public Runnable refrescar = () -> { };

    public Sesion(Biblioteca biblioteca) { this.biblioteca = biblioteca; }
}
