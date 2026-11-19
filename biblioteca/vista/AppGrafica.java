package biblioteca.vista;

import biblioteca.persistencia.Respaldo;
import biblioteca.servicio.Biblioteca;
import biblioteca.servicio.DatosEjemplo;

import javax.swing.*;

/** Punto de entrada del modo gráfico (Swing). */
public class AppGrafica {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
                    if ("Nimbus".equals(info.getName())) UIManager.setLookAndFeel(info.getClassName());
            } catch (Exception ignorada) {
                // se usa el aspecto por defecto
            }
            new VentanaPrincipal(cargarBiblioteca()).setVisible(true);
        });
    }

    private static Biblioteca cargarBiblioteca() {
        if (Respaldo.existe(VentanaPrincipal.ARCHIVO_DATOS)) {
            try {
                return Respaldo.cargar(VentanaPrincipal.ARCHIVO_DATOS);
            } catch (Exception e) {
                Mensajes.aviso(null, "No se pudo leer los datos guardados; se usarán datos de ejemplo.\n" + e.getMessage());
            }
        }
        return DatosEjemplo.crear();
    }
}
