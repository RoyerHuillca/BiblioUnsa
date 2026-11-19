package biblioteca.vista;

import biblioteca.servicio.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/** Dibuja los paneles reales de la interfaz en imágenes PNG (sin abrir ventanas). */
public class CapturasGrafico {
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

    static void guardar(JComponent c, int ancho, int alto, String nombre) throws Exception {
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
        ImageIO.write(img, "png", new File("docs/capturas_gui/" + nombre + ".png"));
        System.out.println("captura: " + nombre);
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels())
            if ("Nimbus".equals(i.getName())) UIManager.setLookAndFeel(i.getClassName());

        SwingUtilities.invokeAndWait(() -> {
            try {
                Biblioteca bib = DatosEjemplo.crear();
                bib.reservarMaterial("E001", "L003");      // una reserva para que se vea en la tabla
                Sesion sesion = new Sesion(bib);
                JPanel[] paneles = {new PanelMateriales(sesion), new PanelUsuarios(sesion),
                        new PanelPrestamos(sesion), new PanelReportes(sesion)};
                String[] titulos = {"Materiales", "Usuarios", "Préstamos y reservas", "Reportes"};
                String[] archivos = {"g01_materiales", "g02_usuarios", "g03_prestamos", "g04_reportes"};
                for (int i = 0; i < paneles.length; i++) {
                    JTabbedPane pestanas = new JTabbedPane();
                    for (int j = 0; j < paneles.length; j++) pestanas.addTab(titulos[j], j == i ? paneles[j] : new JPanel());
                    pestanas.setSelectedIndex(i);
                    guardar(pestanas, 1060, 560, archivos[i]);
                }
                FormularioMaterial fm = new FormularioMaterial();
                fm.txtCodigo.setText("T003");
                fm.txtTitulo.setText("Sistema de gestión de bibliotecas");
                fm.txtAutor.setText("Ana Condori");
                fm.txtAnio.setText("2026");
                fm.cmbTipo.setSelectedItem("Tesis");
                fm.txtExtra1.setText("Dra. Rosa Flores");
                fm.txtExtra2.setText("Ingeniera de Sistemas");
                JPanel marco = new JPanel(new BorderLayout());
                marco.setBorder(BorderFactory.createTitledBorder("Nuevo material"));
                marco.add(fm, BorderLayout.CENTER);
                JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                botones.add(new JButton("Cancelar"));
                botones.add(new JButton("Guardar"));
                marco.add(botones, BorderLayout.SOUTH);
                guardar(marco, 420, 330, "g05_formulario_material");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
