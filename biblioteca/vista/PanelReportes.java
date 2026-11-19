package biblioteca.vista;

import biblioteca.modelo.*;
import biblioteca.servicio.BibliotecaException;
import biblioteca.servicio.Reporte;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.temporal.ChronoUnit;

/** Pestaña de reportes: libros más prestados, usuarios con deuda y préstamos vencidos. */
public class PanelReportes extends JPanel implements Actualizable {
    final Sesion sesion;
    final DefaultTableModel modeloTop = Util.modelo("Posición", "Código", "Título", "Tipo", "Veces prestado");
    final DefaultTableModel modeloDeuda = Util.modelo("Código", "Usuario", "Tipo", "Deuda");
    final DefaultTableModel modeloVencidos = Util.modelo("Usuario", "Material", "Vencía el", "Días de retraso");
    final JButton btnExportar = new JButton("Exportar reporte a .txt");

    public PanelReportes(Sesion sesion) {
        super(new BorderLayout());
        this.sesion = sesion;
        btnExportar.addActionListener(e -> elegirArchivoYExportar());
        JPanel centro = new JPanel(new GridLayout(3, 1, 0, 6));
        centro.add(seccion("Libros más prestados", modeloTop));
        centro.add(seccion("Usuarios con deuda", modeloDeuda));
        centro.add(seccion("Préstamos vencidos", modeloVencidos));
        add(Util.barra(btnExportar), BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        actualizar();
    }

    private JPanel seccion(String titulo, DefaultTableModel modelo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(titulo));
        JTable t = Util.tabla(modelo);
        t.setAutoCreateRowSorter(false);
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        return p;
    }

    @Override
    public void actualizar() {
        Reporte reporte = new Reporte(sesion.biblioteca);
        modeloTop.setRowCount(0);
        int pos = 1;
        for (Material m : reporte.librosMasPrestados())
            modeloTop.addRow(new Object[]{pos++, m.getCodigo(), m.getTitulo(), m.obtenerTipo(), reporte.cantidadPrestamos(m)});
        modeloDeuda.setRowCount(0);
        for (Usuario u : reporte.usuariosConDeuda())
            modeloDeuda.addRow(new Object[]{u.getCodigo(), u.getNombre(), u.obtenerTipo(), Util.soles(sesion.biblioteca.deudaDe(u))});
        modeloVencidos.setRowCount(0);
        for (Prestamo p : reporte.prestamosVencidos())
            modeloVencidos.addRow(new Object[]{p.getUsuario().getNombre(), p.getMaterial().getTitulo(),
                    Util.fecha(p.getFechaVencimiento()), ChronoUnit.DAYS.between(p.getFechaVencimiento(), sesion.biblioteca.hoy())});
    }

    private void elegirArchivoYExportar() {
        JFileChooser selector = new JFileChooser(new File("."));
        selector.setSelectedFile(new File("reporte_biblioteca.txt"));
        selector.setFileFilter(new FileNameExtensionFilter("Texto (*.txt)", "txt"));
        if (selector.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) exportarA(selector.getSelectedFile());
    }

    /** Exporta el reporte a un archivo .txt (se separa del selector para poder probarlo). */
    void exportarA(File archivo) {
        File destino = archivo.getName().toLowerCase().endsWith(".txt") ? archivo : new File(archivo.getPath() + ".txt");
        try {
            new Reporte(sesion.biblioteca).exportarReporteTXT(destino.getPath());
            Mensajes.info(this, "Reporte exportado en:\n" + destino.getAbsolutePath());
        } catch (BibliotecaException ex) {
            Mensajes.aviso(this, ex.getMessage());
        }
    }
}
