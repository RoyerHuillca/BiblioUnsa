package biblioteca.vista;

import biblioteca.persistencia.Respaldo;
import biblioteca.servicio.Biblioteca;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Ventana principal (JFrame) de BiblioUnsa con las pestañas del sistema. */
public class VentanaPrincipal extends JFrame {
    public static final String ARCHIVO_DATOS = "datos/biblioteca.dat";

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
        setJMenuBar(crearMenu());
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

    private JMenuBar crearMenu() {
        JMenu archivo = new JMenu("Archivo");
        archivo.add(item("Guardar ahora", KeyEvent.VK_S, e -> guardar(true)));
        archivo.addSeparator();
        archivo.add(item("Crear copia de seguridad...", 0, e -> crearCopia()));
        archivo.add(item("Restaurar copia de seguridad...", 0, e -> restaurarCopia()));
        archivo.addSeparator();
        archivo.add(item("Salir", 0, e -> salir()));
        JMenu ayuda = new JMenu("Ayuda");
        ayuda.add(item("Acerca de", 0, e -> acercaDe()));
        JMenuBar barra = new JMenuBar();
        barra.add(archivo);
        barra.add(ayuda);
        return barra;
    }

    private JMenuItem item(String texto, int tecla, java.awt.event.ActionListener accion) {
        JMenuItem it = new JMenuItem(texto);
        if (tecla != 0) it.setAccelerator(KeyStroke.getKeyStroke(tecla, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        it.addActionListener(accion);
        return it;
    }

    private boolean guardar(boolean avisar) {
        try {
            Respaldo.guardar(sesion.biblioteca, ARCHIVO_DATOS);
            if (avisar) Mensajes.info(this, "Datos guardados en " + ARCHIVO_DATOS);
            return true;
        } catch (Exception ex) {
            Mensajes.aviso(this, "No se pudo guardar: " + ex.getMessage());
            return false;
        }
    }

    private void crearCopia() {
        JFileChooser fc = new JFileChooser(new File("datos"));
        fc.setSelectedFile(new File("copia_seguridad.dat"));
        fc.setFileFilter(new FileNameExtensionFilter("Copia de seguridad (*.dat)", "dat"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            Respaldo.guardar(sesion.biblioteca, fc.getSelectedFile().getPath());
            Mensajes.info(this, "Copia creada en:\n" + fc.getSelectedFile().getAbsolutePath());
        } catch (Exception ex) {
            Mensajes.aviso(this, "No se pudo crear la copia: " + ex.getMessage());
        }
    }

    private void restaurarCopia() {
        JFileChooser fc = new JFileChooser(new File("datos"));
        fc.setFileFilter(new FileNameExtensionFilter("Copia de seguridad (*.dat)", "dat"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        if (!Mensajes.confirmar(this, "Se reemplazarán los datos actuales por los de la copia. ¿Continuar?")) return;
        try {
            sesion.biblioteca = Respaldo.cargar(fc.getSelectedFile().getPath());
            actualizarTodo();
        } catch (Exception ex) {
            Mensajes.aviso(this, "No se pudo restaurar la copia: " + ex.getMessage());
        }
    }

    private void acercaDe() {
        JDialog d = new JDialog(this, "Acerca de BiblioUnsa", true);
        JLabel texto = new JLabel("<html><div style='padding:12px'><b>BiblioUnsa</b><br>Sistema de gestión de biblioteca universitaria"
                + "<br>Desarrollo de Software Orientado a Objetos - EPIS, UNSA 2026</div></html>");
        JButton cerrar = new JButton("Cerrar");
        cerrar.addActionListener(e -> d.dispose());
        JPanel pie = new JPanel();
        pie.add(cerrar);
        d.add(texto, BorderLayout.CENTER);
        d.add(pie, BorderLayout.SOUTH);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void salir() {
        guardar(false);
        dispose();
        System.exit(0);
    }
}
