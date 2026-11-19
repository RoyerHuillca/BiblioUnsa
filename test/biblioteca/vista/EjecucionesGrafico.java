package biblioteca.vista;

import biblioteca.modelo.*;
import biblioteca.servicio.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;

/**
 * Ejecuta escenas reales de la interfaz (se pulsan los botones con doClick) y guarda una imagen
 * por escena, con el mensaje que la ventana habría mostrado. No abre ventanas.
 */
public class EjecucionesGrafico {
    static String ultimoTexto = "";
    static int ultimoTipo = JOptionPane.INFORMATION_MESSAGE;

    static class SalidaCaptura implements Mensajes.Salida {
        public void info(Component p, String m) { ultimoTexto = m; ultimoTipo = JOptionPane.INFORMATION_MESSAGE; }
        public void aviso(Component p, String m) { ultimoTexto = m; ultimoTipo = JOptionPane.WARNING_MESSAGE; }
        public boolean confirmar(Component p, String m) { return true; }
        public String pedir(Component p, String m, String inicial) { return inicial; }
    }

    static void prepararTablas(Component c) {
        if (c instanceof JTable t) t.addNotify();
        if (c instanceof Container k) for (Component h : k.getComponents()) prepararTablas(h);
    }

    static void distribuir(Component c) {
        if (c instanceof Container k) {
            k.doLayout();
            for (Component h : k.getComponents()) distribuir(h);
        }
    }

    static BufferedImage dibujar(JComponent c, int ancho, int alto) {
        c.setSize(ancho, alto);
        prepararTablas(c);
        distribuir(c);
        distribuir(c);
        BufferedImage img = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, ancho, alto);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        c.paint(g);
        g.dispose();
        return img;
    }

    static BufferedImage pestana(JPanel panel, int indice, int alto) {
        String[] titulos = {"Materiales", "Usuarios", "Préstamos y reservas", "Reportes"};
        JTabbedPane pestanas = new JTabbedPane();
        for (int j = 0; j < titulos.length; j++) pestanas.addTab(titulos[j], j == indice ? panel : new JPanel());
        pestanas.setSelectedIndex(indice);
        return dibujar(pestanas, 1060, alto);
    }

    static BufferedImage mensaje() {
        boolean aviso = ultimoTipo == JOptionPane.WARNING_MESSAGE;
        JOptionPane op = new JOptionPane(ultimoTexto, ultimoTipo, JOptionPane.DEFAULT_OPTION);
        JPanel marco = new JPanel(new BorderLayout());
        marco.setBorder(BorderFactory.createTitledBorder(aviso ? "Aviso" : "Información"));
        marco.add(op, BorderLayout.CENTER);
        Dimension d = marco.getPreferredSize();
        return dibujar(marco, d.width + 20, d.height + 10);
    }

    static void guardar(String nombre, BufferedImage panel, boolean conMensaje) throws Exception {
        BufferedImage msg = conMensaje ? mensaje() : null;
        int alto = panel.getHeight() + (msg == null ? 0 : msg.getHeight() + 24);
        BufferedImage total = new BufferedImage(panel.getWidth(), alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = total.createGraphics();
        g.setColor(new Color(235, 235, 235));
        g.fillRect(0, 0, total.getWidth(), total.getHeight());
        g.drawImage(panel, 0, 0, null);
        if (msg != null) g.drawImage(msg, (total.getWidth() - msg.getWidth()) / 2, panel.getHeight() + 12, null);
        g.dispose();
        ImageIO.write(total, "png", new File("docs/ejecuciones/" + nombre + ".png"));
        System.out.println("escena: " + nombre + (conMensaje ? "  | mensaje: " + ultimoTexto.replace("\n", " ") : ""));
    }

    static void seleccionar(JTable tabla, int columna, String valor) {
        for (int i = 0; i < tabla.getRowCount(); i++)
            if (valor.equals(String.valueOf(tabla.getValueAt(i, columna)))) { tabla.setRowSelectionInterval(i, i); return; }
        throw new IllegalStateException("No está la fila " + valor);
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels())
            if ("Nimbus".equals(i.getName())) UIManager.setLookAndFeel(i.getClassName());
        Mensajes.usar(new SalidaCaptura());

        SwingUtilities.invokeAndWait(() -> {
            try {
                Biblioteca b = DatosEjemplo.crear();
                Sesion sesion = new Sesion(b);
                PanelMateriales pm = new PanelMateriales(sesion);
                PanelUsuarios pu = new PanelUsuarios(sesion);
                PanelPrestamos pp = new PanelPrestamos(sesion);
                PanelReportes pr = new PanelReportes(sesion);
                sesion.refrescar = () -> { pm.actualizar(); pu.actualizar(); pp.actualizar(); pr.actualizar(); };

                // E1: búsqueda
                pm.txtBusqueda.setText("garcia");
                guardar("e01_busqueda_garcia", pestana(pm, 0, 300), false);
                pm.txtBusqueda.setText("");

                // E2: registrar un material
                pm.registrar(new Libro("L050", "Estructuras de Datos y Algoritmos", "Niklaus Wirth", 1986, "978-0131983786", "Prentice Hall"));
                seleccionar(pm.tabla, 0, "L050");
                guardar("e02_material_registrado", pestana(pm, 0, 360), false);

                // E3: prestar a un usuario con multa pendiente
                pp.cmbUsuario.setSelectedItem(b.buscarUsuario("E002"));
                pp.cmbMaterial.setSelectedItem(b.buscarMaterialPorCodigo("L004"));
                pp.btnPrestar.doClick();
                guardar("e03_aviso_multa_pendiente", pestana(pp, 2, 540), true);

                // E4: préstamo registrado
                pp.cmbUsuario.setSelectedItem(b.buscarUsuario("E001"));
                pp.btnPrestar.doClick();
                guardar("e04_prestamo_registrado", pestana(pp, 2, 540), true);

                // E5: devolución con multa
                seleccionar(pp.tablaPrestamos, 2, "Introduction to Algorithms");
                pp.btnDevolver.doClick();
                guardar("e05_devolucion_con_multa", pestana(pp, 2, 540), true);

                // E6: pagar multas
                seleccionar(pu.tabla, 0, "E002");
                pu.btnPagar.doClick();
                guardar("e06_pago_de_multas", pestana(pu, 1, 300), true);

                // E7: reportes
                guardar("e07_reportes", pestana(pr, 3, 560), false);

                // E8: exportar el reporte a .txt (el contenido real del archivo)
                File txt = new File("docs/ejecuciones/reporte_biblioteca.txt");
                pr.exportarA(txt);
                System.out.println("texto exportado: " + txt.getPath() + " (" + Files.size(txt.toPath()) + " bytes)");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
