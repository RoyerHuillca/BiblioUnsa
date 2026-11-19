package biblioteca.vista;

import biblioteca.modelo.*;
import biblioteca.servicio.BibliotecaException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Pestaña de préstamos, devoluciones (con multa) y reservas. */
public class PanelPrestamos extends JPanel implements Actualizable {
    final Sesion sesion;
    final JComboBox<Usuario> cmbUsuario = new JComboBox<>();
    final JComboBox<Material> cmbMaterial = new JComboBox<>();
    final JCheckBox chkSoloActivos = new JCheckBox("Solo activos");
    final JButton btnPrestar = new JButton("Registrar préstamo"), btnReservar = new JButton("Reservar"),
            btnDevolver = new JButton("Registrar devolución"), btnCancelar = new JButton("Cancelar reserva");
    final DefaultTableModel modeloPrestamos = Util.modelo("N.º", "Usuario", "Material", "Vence", "Devuelto", "Estado", "Multa");
    final JTable tablaPrestamos = Util.tabla(modeloPrestamos);
    final DefaultTableModel modeloReservas = Util.modelo("Usuario", "Material", "Fecha de reserva", "Estado del material");
    final JTable tablaReservas = Util.tabla(modeloReservas);
    private final List<Prestamo> filasPrestamos = new ArrayList<>();
    private final List<Reserva> filasReservas = new ArrayList<>();

    public PanelPrestamos(Sesion sesion) {
        super(new BorderLayout());
        this.sesion = sesion;
        btnPrestar.addActionListener(e -> prestar());
        btnReservar.addActionListener(e -> reservar());
        btnDevolver.addActionListener(e -> devolver());
        btnCancelar.addActionListener(e -> cancelarReserva());
        chkSoloActivos.addActionListener(e -> actualizar());
        cmbUsuario.setPreferredSize(new Dimension(240, 26));
        cmbMaterial.setPreferredSize(new Dimension(300, 26));

        JPanel panelPrestamos = new JPanel(new BorderLayout());
        panelPrestamos.setBorder(BorderFactory.createTitledBorder("Préstamos"));
        panelPrestamos.add(Util.barra(btnDevolver, chkSoloActivos), BorderLayout.NORTH);
        panelPrestamos.add(new JScrollPane(tablaPrestamos), BorderLayout.CENTER);

        JPanel panelReservas = new JPanel(new BorderLayout());
        panelReservas.setBorder(BorderFactory.createTitledBorder("Reservas pendientes"));
        panelReservas.add(Util.barra(btnCancelar), BorderLayout.NORTH);
        panelReservas.add(new JScrollPane(tablaReservas), BorderLayout.CENTER);

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelPrestamos, panelReservas);
        division.setResizeWeight(0.65);
        add(Util.barra(new JLabel("Usuario:"), cmbUsuario, new JLabel("Material:"), cmbMaterial, btnPrestar, btnReservar),
                BorderLayout.NORTH);
        add(division, BorderLayout.CENTER);
        Util.anchos(tablaPrestamos, 40, 170, 240, 85, 85, 80, 150);
        actualizar();
    }

    @Override
    public void actualizar() {
        Usuario uSel = (Usuario) cmbUsuario.getSelectedItem();
        Material mSel = (Material) cmbMaterial.getSelectedItem();
        cmbUsuario.removeAllItems();
        for (Usuario u : sesion.biblioteca.getUsuarios()) cmbUsuario.addItem(u);
        cmbMaterial.removeAllItems();
        for (Material m : sesion.biblioteca.getMateriales()) cmbMaterial.addItem(m);
        if (uSel != null) cmbUsuario.setSelectedItem(sesion.biblioteca.buscarUsuario(uSel.getCodigo()));
        if (mSel != null) cmbMaterial.setSelectedItem(sesion.biblioteca.buscarMaterialPorCodigo(mSel.getCodigo()));

        modeloPrestamos.setRowCount(0);
        filasPrestamos.clear();
        int n = 1;
        for (Prestamo p : sesion.biblioteca.getPrestamos()) {
            p.estaVencido(sesion.biblioteca.hoy());   // marca como VENCIDO si corresponde
            int numero = n++;
            if (chkSoloActivos.isSelected() && p.estaDevuelto()) continue;
            Multa m = p.getMulta();
            String multa = m == null ? "-" : Util.soles(m.getMonto()) + (m.isPagada() ? " (pagada)" : " (pendiente)");
            modeloPrestamos.addRow(new Object[]{numero, p.getUsuario().getNombre(), p.getMaterial().getTitulo(),
                    Util.fecha(p.getFechaVencimiento()), Util.fecha(p.getFechaDevolucion()), p.getEstado(), multa});
            filasPrestamos.add(p);
        }

        modeloReservas.setRowCount(0);
        filasReservas.clear();
        for (Reserva r : sesion.biblioteca.reservasPendientes()) {
            modeloReservas.addRow(new Object[]{r.getUsuario().getNombre(), r.getMaterial().getTitulo(),
                    Util.fecha(r.getFechaReserva()), r.getMaterial().isDisponible() ? "Disponible" : "Prestado"});
            filasReservas.add(r);
        }
    }

    void prestar() {
        Usuario u = (Usuario) cmbUsuario.getSelectedItem();
        Material m = (Material) cmbMaterial.getSelectedItem();
        if (u == null || m == null) { Mensajes.aviso(this, "Seleccione un usuario y un material."); return; }
        try {
            Prestamo p = sesion.biblioteca.registrarPrestamo(u.getCodigo(), m.getCodigo());
            sesion.refrescar.run();
            Mensajes.info(this, "Préstamo registrado. Devolver hasta el " + Util.fecha(p.getFechaVencimiento()) + ".");
        } catch (BibliotecaException ex) {
            Mensajes.aviso(this, ex.getMessage());
        }
    }

    void reservar() {
        Usuario u = (Usuario) cmbUsuario.getSelectedItem();
        Material m = (Material) cmbMaterial.getSelectedItem();
        if (u == null || m == null) { Mensajes.aviso(this, "Seleccione un usuario y un material."); return; }
        try {
            sesion.biblioteca.reservarMaterial(u.getCodigo(), m.getCodigo());
            sesion.refrescar.run();
            Mensajes.info(this, "Reserva registrada.");
        } catch (BibliotecaException ex) {
            Mensajes.aviso(this, ex.getMessage());
        }
    }

    void devolver() {
        int fila = tablaPrestamos.getSelectedRow();
        if (fila < 0) { Mensajes.aviso(this, "Seleccione un préstamo de la tabla."); return; }
        Prestamo p = filasPrestamos.get(tablaPrestamos.convertRowIndexToModel(fila));
        if (p.estaDevuelto()) { Mensajes.aviso(this, "Ese préstamo ya fue devuelto."); return; }
        try {
            Multa multa = sesion.biblioteca.registrarDevolucion(p.getMaterial().getCodigo());
            sesion.refrescar.run();
            if (multa == null) Mensajes.info(this, "Devolución a tiempo. Sin multa.");
            else Mensajes.info(this, "Devolución con " + multa.getDiasRetraso() + " día(s) de retraso.\nMulta: " + Util.soles(multa.getMonto()) + ".");
        } catch (BibliotecaException ex) {
            Mensajes.aviso(this, ex.getMessage());
        }
    }

    void cancelarReserva() {
        int fila = tablaReservas.getSelectedRow();
        if (fila < 0) { Mensajes.aviso(this, "Seleccione una reserva de la tabla."); return; }
        filasReservas.get(tablaReservas.convertRowIndexToModel(fila)).cancelar();
        sesion.refrescar.run();
    }
}
