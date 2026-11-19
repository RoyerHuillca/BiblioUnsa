package biblioteca.vista;

import biblioteca.modelo.Usuario;

import javax.swing.*;
import java.awt.*;

/** Ventana (JDialog) con el formulario de usuario. */
public class DialogoUsuario extends JDialog {
    private final FormularioUsuario formulario = new FormularioUsuario();
    private Usuario resultado;

    public DialogoUsuario(Frame dueno) {
        super(dueno, "Nuevo usuario", true);
        JButton guardar = new JButton("Guardar"), cancelar = new JButton("Cancelar");
        guardar.addActionListener(e -> {
            try {
                resultado = formulario.construir();
                dispose();
            } catch (IllegalArgumentException ex) {
                Mensajes.aviso(this, ex.getMessage());
            }
        });
        cancelar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(cancelar);
        botones.add(guardar);
        getRootPane().setDefaultButton(guardar);
        add(formulario, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(dueno);
    }

    public Usuario mostrar() {
        setVisible(true);
        return resultado;
    }
}
