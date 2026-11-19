package biblioteca.vista;

import biblioteca.modelo.*;

import javax.swing.*;
import java.awt.*;

/** Formulario para registrar un libro, una tesis o una revista. No abre ventanas: solo arma el objeto. */
public class FormularioMaterial extends JPanel {
    final JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"Libro", "Tesis", "Revista"});
    final JTextField txtCodigo = new JTextField(18), txtTitulo = new JTextField(18), txtAutor = new JTextField(18),
            txtAnio = new JTextField(18), txtExtra1 = new JTextField(18), txtExtra2 = new JTextField(18),
            txtExtra3 = new JTextField(18);
    private final JLabel lblExtra1 = new JLabel(), lblExtra2 = new JLabel(), lblExtra3 = new JLabel();

    public FormularioMaterial() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));
        fila(0, new JLabel("Tipo"), cmbTipo);
        fila(1, new JLabel("Código"), txtCodigo);
        fila(2, new JLabel("Título"), txtTitulo);
        fila(3, new JLabel("Autor"), txtAutor);
        fila(4, new JLabel("Año"), txtAnio);
        fila(5, lblExtra1, txtExtra1);
        fila(6, lblExtra2, txtExtra2);
        fila(7, lblExtra3, txtExtra3);
        cmbTipo.addActionListener(e -> actualizarEtiquetas());
        actualizarEtiquetas();
    }

    private void fila(int f, JLabel etiqueta, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = f; c.insets = new Insets(4, 4, 4, 4); c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; add(etiqueta, c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; add(campo, c);
    }

    private void actualizarEtiquetas() {
        String tipo = (String) cmbTipo.getSelectedItem();
        boolean revista = "Revista".equals(tipo);
        switch (tipo) {
            case "Libro" -> { lblExtra1.setText("ISBN"); lblExtra2.setText("Editorial"); }
            case "Tesis" -> { lblExtra1.setText("Asesor"); lblExtra2.setText("Grado"); }
            default -> { lblExtra1.setText("ISSN"); lblExtra2.setText("Volumen"); lblExtra3.setText("Número"); }
        }
        lblExtra3.setVisible(revista);
        txtExtra3.setVisible(revista);
        revalidate();
        repaint();
    }

    /** Construye el material con los datos escritos o lanza IllegalArgumentException si hay un error. */
    public Material construir() {
        String codigo = txtCodigo.getText().trim(), titulo = txtTitulo.getText().trim(), autor = txtAutor.getText().trim();
        if (codigo.isEmpty()) throw new IllegalArgumentException("El código es obligatorio.");
        if (titulo.isEmpty()) throw new IllegalArgumentException("El título es obligatorio.");
        int anio = entero(txtAnio.getText(), "El año");
        String e1 = txtExtra1.getText().trim(), e2 = txtExtra2.getText().trim();
        switch ((String) cmbTipo.getSelectedItem()) {
            case "Libro": return new Libro(codigo, titulo, autor, anio, e1, e2);
            case "Tesis": return new Tesis(codigo, titulo, autor, anio, e1, e2);
            default: return new Revista(codigo, titulo, autor, anio, e1, entero(e2, "El volumen"), entero(txtExtra3.getText(), "El número"));
        }
    }

    private static int entero(String texto, String campo) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(campo + " debe ser un número entero.");
        }
    }
}
