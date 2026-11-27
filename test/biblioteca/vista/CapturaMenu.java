package biblioteca.vista;

import biblioteca.servicio.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/** Dibuja la barra de menú real (BarraMenu) con el menú Archivo desplegado. No abre ventanas. */
public class CapturaMenu {
    static void distribuir(Component c) {
        if (c instanceof Container k) {
            k.doLayout();
            for (Component h : k.getComponents()) distribuir(h);
        }
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels())
            if ("Nimbus".equals(i.getName())) UIManager.setLookAndFeel(i.getClassName());
        SwingUtilities.invokeAndWait(() -> {
            try {
                BarraMenu barra = new BarraMenu(new Sesion(DatosEjemplo.crear()), null, () -> { });
                JPopupMenu popup = barra.getMenu(0).getPopupMenu();
                Dimension dp = popup.getPreferredSize();
                popup.setSize(dp);
                distribuir(popup);
                barra.setSize(430, 30);
                distribuir(barra);
                barra.getMenu(0).setSelected(true);
                BufferedImage img = new BufferedImage(430, 30 + dp.height + 12, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = img.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, img.getWidth(), img.getHeight());
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                barra.paint(g);
                g.translate(0, 30);
                popup.paint(g);
                g.dispose();
                ImageIO.write(img, "png", new File("docs/sprint3/s1_menu_archivo.png"));
                System.out.println("menú dibujado: " + img.getWidth() + "x" + img.getHeight());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
