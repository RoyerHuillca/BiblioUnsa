package biblioteca.vista;

import biblioteca.modelo.*;

import javax.swing.*;
import java.awt.*;

/** Formulario para registrar un estudiante, un docente o un administrativo. */
public class FormularioUsuario extends JPanel {
    final JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"Estudiante", "Docente", "Administrativo"});
    final JTextField txtCodigo = new JTextField(18), txtNombre = new JTextField(18),
            txtCorreo = new JTextField(18), txtExtra = new JTextField(18);
    private final JLabel lblExtra = new JLabel();

    public FormularioUsuario() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));
        fila(0, new JLabel("Tipo"), cmbTipo);
        fila(1, new JLabel("Código"), txtCodigo);
        fila(2, new JLabel("Nombre completo"), txtNombre);
        fila(3, new JLabel("Correo"), txtCorreo);
        fila(4, lblExtra, txtExtra);
        cmbTipo.addActionListener(e -> actualizarEtiqueta());
        actualizarEtiqueta();
    }

    private void fila(int f, JLabel etiqueta, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = f; c.insets = new Insets(4, 4, 4, 4); c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; add(etiqueta, c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; add(campo, c);
    }

    private void actualizarEtiqueta() {
        switch ((String) cmbTipo.getSelectedItem()) {
            case "Estudiante" -> lblExtra.setText("Escuela");
            case "Docente" -> lblExtra.setText("Departamento");
            default -> lblExtra.setText("Área");
        }
    }

    public Usuario construir() {
        String codigo = txtCodigo.getText().trim(), nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim(), extra = txtExtra.getText().trim();
        if (codigo.isEmpty()) throw new IllegalArgumentException("El código es obligatorio.");
        if (nombre.isEmpty()) throw new IllegalArgumentException("El nombre es obligatorio.");
        switch ((String) cmbTipo.getSelectedItem()) {
            case "Estudiante": return new Estudiante(codigo, nombre, correo, extra);
            case "Docente": return new Docente(codigo, nombre, correo, extra);
            default: return new Administrativo(codigo, nombre, correo, extra);
        }
    }
}
