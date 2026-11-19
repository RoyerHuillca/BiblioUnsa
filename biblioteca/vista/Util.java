package biblioteca.vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

final class Util {
    static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Util() { }

    static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
    }

    static JTable tabla(DefaultTableModel modelo) {
        JTable t = new JTable(modelo);
        t.setRowHeight(24);
        t.setFillsViewportHeight(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoCreateRowSorter(true);
        return t;
    }

    static void anchos(JTable t, int... anchos) {
        for (int i = 0; i < anchos.length; i++) t.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
    }

    static String fecha(LocalDate f) { return f == null ? "-" : f.format(FECHA); }

    static String soles(double monto) { return String.format("S/ %.2f", monto); }

    static JPanel barra(Component... componentes) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        for (Component c : componentes) p.add(c);
        return p;
    }
}
