package biblioteca.vista;

import javax.swing.JOptionPane;
import java.awt.Component;

/**
 * Punto único para mostrar mensajes al usuario. En la aplicación usa cuadros de diálogo de Swing;
 * en las pruebas automáticas se reemplaza por una salida falsa para no abrir ventanas.
 */
public final class Mensajes {

    public interface Salida {
        void info(Component padre, String mensaje);
        void aviso(Component padre, String mensaje);
        boolean confirmar(Component padre, String mensaje);
        /** Pide un texto al usuario; devuelve null si cancela. */
        String pedir(Component padre, String mensaje, String inicial);
    }

    private static Salida salida = new SalidaSwing();

    private Mensajes() { }

    public static void usar(Salida nueva) { salida = nueva; }

    public static void info(Component padre, String mensaje) { salida.info(padre, mensaje); }
    public static void aviso(Component padre, String mensaje) { salida.aviso(padre, mensaje); }
    public static boolean confirmar(Component padre, String mensaje) { return salida.confirmar(padre, mensaje); }
    public static String pedir(Component padre, String mensaje, String inicial) { return salida.pedir(padre, mensaje, inicial); }

    private static class SalidaSwing implements Salida {
        @Override public void info(Component padre, String mensaje) {
            JOptionPane.showMessageDialog(padre, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
        }
        @Override public void aviso(Component padre, String mensaje) {
            JOptionPane.showMessageDialog(padre, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
        }
        @Override public boolean confirmar(Component padre, String mensaje) {
            return JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        }
        @Override public String pedir(Component padre, String mensaje, String inicial) {
            return (String) JOptionPane.showInputDialog(padre, mensaje, "Editar", JOptionPane.PLAIN_MESSAGE, null, null, inicial);
        }
    }
}
