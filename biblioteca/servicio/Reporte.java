package biblioteca.servicio;

import biblioteca.modelo.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Genera los reportes a partir de los datos de la biblioteca y los exporta a un archivo de texto. */
public class Reporte implements Exportable {
    private Biblioteca biblioteca;

    public Reporte(Biblioteca biblioteca) { this.biblioteca = biblioteca; }

    /** Materiales prestados al menos una vez, del más prestado al menos prestado. */
    public List<Material> librosMasPrestados() {
        List<Material> lista = new ArrayList<>();
        for (Material m : biblioteca.getMateriales()) if (cantidadPrestamos(m) > 0) lista.add(m);
        lista.sort((a, b) -> cantidadPrestamos(b) - cantidadPrestamos(a));
        return lista;
    }

    public int cantidadPrestamos(Material m) {
        int veces = 0;
        for (Prestamo p : biblioteca.getPrestamos()) if (p.getMaterial() == m) veces++;
        return veces;
    }

    public List<Usuario> usuariosConDeuda() {
        List<Usuario> lista = new ArrayList<>();
        for (Usuario u : biblioteca.getUsuarios()) if (biblioteca.deudaDe(u) > 0) lista.add(u);
        return lista;
    }

    public List<Prestamo> prestamosVencidos() { return biblioteca.obtenerPrestamosVencidos(); }

    public String generarTexto() {
        StringBuilder s = new StringBuilder();
        s.append("REPORTES - ").append(biblioteca.getNombre()).append("\nFecha: ").append(biblioteca.hoy()).append("\n");

        s.append("\n== Materiales más prestados ==\n");
        List<Material> top = librosMasPrestados();
        if (top.isEmpty()) s.append("(sin préstamos)\n");
        for (Material m : top)
            s.append(String.format("%-6s %-40s %d vez/veces%n", m.getCodigo(), m.getTitulo(), cantidadPrestamos(m)));

        s.append("\n== Usuarios con deuda ==\n");
        List<Usuario> deudores = usuariosConDeuda();
        if (deudores.isEmpty()) s.append("(ninguno)\n");
        for (Usuario u : deudores)
            s.append(String.format("%-6s %-28s S/ %.2f%n", u.getCodigo(), u.getNombre(), biblioteca.deudaDe(u)));

        s.append("\n== Préstamos vencidos ==\n");
        List<Prestamo> vencidos = prestamosVencidos();
        if (vencidos.isEmpty()) s.append("(ninguno)\n");
        for (Prestamo p : vencidos)
            s.append(String.format("%-28s %-36s vencía el %s%n", p.getUsuario().getNombre(),
                    p.getMaterial().getTitulo(), p.getFechaVencimiento()));
        return s.toString();
    }

    @Override
    public void exportarReporteTXT(String ruta) {
        try {
            Files.writeString(Paths.get(ruta), generarTexto(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BibliotecaException("No se pudo exportar el reporte: " + e.getMessage());
        }
    }
}
