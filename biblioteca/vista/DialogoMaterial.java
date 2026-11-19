package biblioteca.vista;

import biblioteca.modelo.Material;

import javax.swing.*;
import java.awt.*;

/** Ventana (JDialog) que muestra el formulario de material con los botones Guardar y Cancelar. */
public class DialogoMaterial extends JDialog {
    private final FormularioMaterial formulario = new FormularioMaterial();
    private Material resultado;

    public DialogoMaterial(Frame dueno) {
        super(dueno, "Nuevo material", true);
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

    /** Muestra la ventana y devuelve el material creado, o null si se canceló. */
    public Material mostrar() {
        setVisible(true);
        return resultado;
    }
}
