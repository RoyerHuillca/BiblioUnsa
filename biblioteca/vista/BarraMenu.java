package biblioteca.vista;

import biblioteca.persistencia.Respaldo;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

/** Barra de menú de la ventana: Archivo (guardar, copias de seguridad, base de datos, salir) y Ayuda. */
public class BarraMenu extends JMenuBar {
    public static final String ARCHIVO_DATOS = "datos/biblioteca.dat";

    private final Sesion sesion;
    private final Component padre;
    final JMenuItem itemGuardar, itemCopia, itemRestaurar, itemGuardarBD, itemCargarBD, itemSalir, itemAcercaDe;

    public BarraMenu(Sesion sesion, Component padre, Runnable alSalir) {
        this.sesion = sesion;
        this.padre = padre;
        JMenu archivo = new JMenu("Archivo");
        itemGuardar = item("Guardar ahora", KeyEvent.VK_S, e -> guardarDatos(sesion, padre, true));
        itemCopia = item("Crear copia de seguridad...", 0, e -> crearCopia());
        itemRestaurar = item("Restaurar copia de seguridad...", 0, e -> restaurarCopia());
        itemGuardarBD = item("Guardar en base de datos (SQLite)", 0, e -> AccionesBD.guardar(sesion, padre));
        itemCargarBD = item("Cargar desde base de datos (SQLite)", 0, e -> AccionesBD.cargar(sesion, padre));
        itemSalir = item("Salir", 0, e -> alSalir.run());
        archivo.add(itemGuardar);
        archivo.addSeparator();
        archivo.add(itemCopia);
        archivo.add(itemRestaurar);
        archivo.addSeparator();
        archivo.add(itemGuardarBD);
        archivo.add(itemCargarBD);
        archivo.addSeparator();
        archivo.add(itemSalir);

        JMenu ayuda = new JMenu("Ayuda");
        itemAcercaDe = item("Acerca de", 0, e -> acercaDe());
        ayuda.add(itemAcercaDe);
        add(archivo);
        add(ayuda);
    }

    private static JMenuItem item(String texto, int tecla, java.awt.event.ActionListener accion) {
        JMenuItem it = new JMenuItem(texto);
        if (tecla != 0) {
            int mascara;
            try {
                mascara = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
            } catch (HeadlessException e) {          // sin pantalla (pruebas automáticas)
                mascara = InputEvent.CTRL_DOWN_MASK;
            }
            it.setAccelerator(KeyStroke.getKeyStroke(tecla, mascara));
        }
        it.addActionListener(accion);
        return it;
    }

    /** Guarda la biblioteca en el archivo de datos del programa (serialización). */
    static boolean guardarDatos(Sesion sesion, Component padre, boolean avisar) {
        try {
            Respaldo.guardar(sesion.biblioteca, ARCHIVO_DATOS);
            if (avisar) Mensajes.info(padre, "Datos guardados en " + ARCHIVO_DATOS);
            return true;
        } catch (Exception ex) {
            Mensajes.aviso(padre, "No se pudo guardar: " + ex.getMessage());
            return false;
        }
    }

    private void crearCopia() {
        JFileChooser fc = new JFileChooser(new File("datos"));
        fc.setSelectedFile(new File("copia_seguridad.dat"));
        fc.setFileFilter(new FileNameExtensionFilter("Copia de seguridad (*.dat)", "dat"));
        if (fc.showSaveDialog(padre) != JFileChooser.APPROVE_OPTION) return;
        try {
            Respaldo.guardar(sesion.biblioteca, fc.getSelectedFile().getPath());
            Mensajes.info(padre, "Copia creada en:\n" + fc.getSelectedFile().getAbsolutePath());
        } catch (Exception ex) {
            Mensajes.aviso(padre, "No se pudo crear la copia: " + ex.getMessage());
        }
    }

    private void restaurarCopia() {
        JFileChooser fc = new JFileChooser(new File("datos"));
        fc.setFileFilter(new FileNameExtensionFilter("Copia de seguridad (*.dat)", "dat"));
        if (fc.showOpenDialog(padre) != JFileChooser.APPROVE_OPTION) return;
        if (!Mensajes.confirmar(padre, "Se reemplazarán los datos actuales por los de la copia. ¿Continuar?")) return;
        try {
            sesion.biblioteca = Respaldo.cargar(fc.getSelectedFile().getPath());
            sesion.refrescar.run();
        } catch (Exception ex) {
            Mensajes.aviso(padre, "No se pudo restaurar la copia: " + ex.getMessage());
        }
    }

    private void acercaDe() {
        Window dueno = padre == null ? null : SwingUtilities.getWindowAncestor(padre);
        JDialog d = new JDialog(dueno instanceof Frame f ? f : null, "Acerca de BiblioUnsa", true);
        JLabel texto = new JLabel("<html><div style='padding:12px'><b>BiblioUnsa</b><br>Sistema de gestión de biblioteca universitaria"
                + "<br>Desarrollo de Software Orientado a Objetos - EPIS, UNSA 2026</div></html>");
        JButton cerrar = new JButton("Cerrar");
        cerrar.addActionListener(e -> d.dispose());
        JPanel pie = new JPanel();
        pie.add(cerrar);
        d.add(texto, BorderLayout.CENTER);
        d.add(pie, BorderLayout.SOUTH);
        d.pack();
        d.setLocationRelativeTo(padre);
        d.setVisible(true);
    }
}
