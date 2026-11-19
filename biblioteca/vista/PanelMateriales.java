package biblioteca.vista;

import biblioteca.modelo.*;
import biblioteca.servicio.BibliotecaException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Pestaña de materiales: lista, búsqueda, registro, edición y baja. */
public class PanelMateriales extends JPanel implements Actualizable {
    final Sesion sesion;
    final DefaultTableModel modelo = Util.modelo("Código", "Tipo", "Título", "Autor", "Año", "Detalle", "Estado");
    final JTable tabla = Util.tabla(modelo);
    final JTextField txtBusqueda = new JTextField(24);
    final JButton btnNuevo = new JButton("Nuevo material"), btnEditar = new JButton("Editar"),
            btnBaja = new JButton("Dar de baja");

    public PanelMateriales(Sesion sesion) {
        super(new BorderLayout());
        this.sesion = sesion;
        btnNuevo.addActionListener(e -> nuevo());
        btnEditar.addActionListener(e -> editar());
        btnBaja.addActionListener(e -> baja());
        txtBusqueda.setToolTipText("Busca por título, autor o código");
        txtBusqueda.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { actualizar(); }
            public void removeUpdate(DocumentEvent e) { actualizar(); }
            public void changedUpdate(DocumentEvent e) { actualizar(); }
        });
        add(Util.barra(new JLabel("Buscar (título, autor o código):"), txtBusqueda, btnNuevo, btnEditar, btnBaja), BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        Util.anchos(tabla, 60, 70, 260, 180, 55, 270, 90);
        actualizar();
    }

    @Override
    public void actualizar() {
        modelo.setRowCount(0);
        for (Material m : sesion.biblioteca.buscarMaterial(txtBusqueda.getText()))
            modelo.addRow(new Object[]{m.getCodigo(), m.obtenerTipo(), m.getTitulo(), m.getAutor(), m.getAnio(),
                    detalle(m), m.isDisponible() ? "Disponible" : "Prestado"});
    }

    private static String detalle(Material m) {
        if (m instanceof Libro l) return "ISBN " + l.getIsbn() + " · " + l.getEditorial();
        if (m instanceof Tesis t) return "Asesor " + t.getAsesor() + " · " + t.getGrado();
        if (m instanceof Revista r) return "ISSN " + r.getIssn() + " · Vol. " + r.getVolumen() + " N.º " + r.getNumero();
        return "";
    }

    Material seleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { Mensajes.aviso(this, "Seleccione un material de la tabla."); return null; }
        return sesion.biblioteca.buscarMaterialPorCodigo((String) tabla.getValueAt(fila, 0));
    }

    void nuevo() {
        Material m = new DialogoMaterial(sesion.ventana).mostrar();
        if (m != null) registrar(m);
    }

    /** Registra el material en la biblioteca y refresca todas las pestañas. */
    void registrar(Material m) {
        try {
            sesion.biblioteca.registrarMaterial(m);
            sesion.refrescar.run();
        } catch (BibliotecaException ex) {
            Mensajes.aviso(this, ex.getMessage());
        }
    }

    void editar() {
        Material m = seleccionado();
        if (m == null) return;
        String titulo = Mensajes.pedir(this, "Título de " + m.getCodigo() + ":", m.getTitulo());
        if (titulo == null) return;
        String autor = Mensajes.pedir(this, "Autor de " + m.getCodigo() + ":", m.getAutor());
        if (autor == null) return;
        if (sesion.biblioteca.editarMaterial(m.getCodigo(), titulo, autor)) sesion.refrescar.run();
        else Mensajes.aviso(this, "No existe un material con ese código.");
    }

    void baja() {
        Material m = seleccionado();
        if (m == null || !Mensajes.confirmar(this, "¿Dar de baja \"" + m.getTitulo() + "\"?")) return;
        if (sesion.biblioteca.eliminarMaterial(m.getCodigo())) sesion.refrescar.run();
        else Mensajes.aviso(this, "No se pudo dar de baja: el material está prestado.");
    }
}
