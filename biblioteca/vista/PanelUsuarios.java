package biblioteca.vista;

import biblioteca.modelo.Usuario;
import biblioteca.servicio.BibliotecaException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Pestaña de usuarios: lista, búsqueda por código, registro y pago de multas. */
public class PanelUsuarios extends JPanel implements Actualizable {
    final Sesion sesion;
    final DefaultTableModel modelo = Util.modelo("Código", "Tipo", "Nombre", "Correo",
            "Préstamos activos", "Límite", "Plazo (días)", "Deuda");
    final JTable tabla = Util.tabla(modelo);
    final JTextField txtBusqueda = new JTextField(12);
    final JButton btnBuscar = new JButton("Buscar"), btnTodos = new JButton("Ver todos"),
            btnNuevo = new JButton("Nuevo usuario"), btnPagar = new JButton("Pagar multas");

    public PanelUsuarios(Sesion sesion) {
        super(new BorderLayout());
        this.sesion = sesion;
        btnBuscar.addActionListener(e -> actualizar());
        btnTodos.addActionListener(e -> { txtBusqueda.setText(""); actualizar(); });
        btnNuevo.addActionListener(e -> nuevo());
        btnPagar.addActionListener(e -> pagar());
        add(Util.barra(new JLabel("Código:"), txtBusqueda, btnBuscar, btnTodos, btnNuevo, btnPagar), BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        Util.anchos(tabla, 60, 100, 190, 190, 110, 60, 90, 80);
        actualizar();
    }

    @Override
    public void actualizar() {
        modelo.setRowCount(0);
        String codigo = txtBusqueda.getText().trim();
        List<Usuario> lista;
        if (codigo.isEmpty()) lista = sesion.biblioteca.getUsuarios();
        else {
            Usuario u = sesion.biblioteca.buscarUsuario(codigo);
            lista = u == null ? List.of() : List.of(u);
        }
        for (Usuario u : lista)
            modelo.addRow(new Object[]{u.getCodigo(), u.obtenerTipo(), u.getNombre(), u.getCorreo(),
                    sesion.biblioteca.prestamosActivosDe(u).size(), u.obtenerLimitePrestamos(),
                    u.obtenerPlazoPrestamo(), Util.soles(sesion.biblioteca.deudaDe(u))});
    }

    void nuevo() {
        Usuario u = new DialogoUsuario(sesion.ventana).mostrar();
        if (u != null) registrar(u);
    }

    void registrar(Usuario u) {
        try {
            sesion.biblioteca.registrarUsuario(u);
            sesion.refrescar.run();
        } catch (BibliotecaException ex) {
            Mensajes.aviso(this, ex.getMessage());
        }
    }

    void pagar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { Mensajes.aviso(this, "Seleccione un usuario de la tabla."); return; }
        String codigo = (String) tabla.getValueAt(fila, 0);
        try {
            double monto = sesion.biblioteca.pagarMultas(codigo);
            sesion.refrescar.run();
            Mensajes.info(this, "Se registró el pago de " + Util.soles(monto) + ".");
        } catch (BibliotecaException ex) {
            Mensajes.aviso(this, ex.getMessage());
        }
    }
}
