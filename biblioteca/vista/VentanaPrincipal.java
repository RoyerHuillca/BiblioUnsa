package biblioteca.vista;

import biblioteca.servicio.Biblioteca;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/** Ventana principal (JFrame) de BiblioUnsa con las pestañas del sistema. */
public class VentanaPrincipal extends JFrame {
    public static final String ARCHIVO_DATOS = BarraMenu.ARCHIVO_DATOS;

    private final Sesion sesion;
    private final List<Actualizable> paneles = new ArrayList<>();
    private final JTabbedPane pestanas = new JTabbedPane();
    private final JLabel estado = new JLabel(" ");

    public VentanaPrincipal(Biblioteca biblioteca) {
        super("BiblioUnsa - Sistema de gestión de biblioteca universitaria");
        sesion = new Sesion(biblioteca);
        sesion.ventana = this;
        sesion.refrescar = this::actualizarTodo;

        agregar("Materiales", new PanelMateriales(sesion));
        agregar("Usuarios", new PanelUsuarios(sesion));
        agregar("Préstamos y reservas", new PanelPrestamos(sesion));
        agregar("Reportes", new PanelReportes(sesion));

        estado.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        setJMenuBar(new BarraMenu(sesion, this, this::salir));
        add(pestanas, BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);
        actualizarEstado();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { salir(); }
        });
        setSize(1060, 640);
        setLocationRelativeTo(null);
    }

    private void agregar(String titulo, JPanel panel) {
        pestanas.addTab(titulo, panel);
        paneles.add((Actualizable) panel);
    }

    private void actualizarTodo() {
        for (Actualizable a : paneles) a.actualizar();
        actualizarEstado();
    }

    private void actualizarEstado() {
        Biblioteca b = sesion.biblioteca;
        estado.setText("  " + b.getMateriales().size() + " materiales  |  " + b.getUsuarios().size() + " usuarios  |  "
                + b.obtenerPrestamosVencidos().size() + " préstamos vencidos  |  Fecha: " + Util.fecha(b.hoy()));
    }

    private void salir() {
        BarraMenu.guardarDatos(sesion, this, false);
        dispose();
        System.exit(0);
    }
}
