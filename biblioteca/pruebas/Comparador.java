package biblioteca.pruebas;

import biblioteca.modelo.*;
import biblioteca.servicio.Biblioteca;

import java.util.ArrayList;
import java.util.List;

/** Compara dos bibliotecas dato por dato y devuelve la lista de diferencias (vacía si son iguales). */
public class Comparador {

    public static List<String> diferencias(Biblioteca a, Biblioteca b) {
        List<String> d = new ArrayList<>();
        a.obtenerPrestamosVencidos();   // marca los VENCIDO en las dos para comparar el mismo estado
        b.obtenerPrestamosVencidos();

        if (a.getMateriales().size() != b.getMateriales().size()) d.add("cantidad de materiales");
        else for (int i = 0; i < a.getMateriales().size(); i++) comparar(d, a.getMateriales().get(i), b.getMateriales().get(i));

        if (a.getUsuarios().size() != b.getUsuarios().size()) d.add("cantidad de usuarios");
        else for (int i = 0; i < a.getUsuarios().size(); i++) comparar(d, a.getUsuarios().get(i), b.getUsuarios().get(i));

        if (a.getPrestamos().size() != b.getPrestamos().size()) d.add("cantidad de préstamos");
        else for (int i = 0; i < a.getPrestamos().size(); i++) comparar(d, i + 1, a.getPrestamos().get(i), b.getPrestamos().get(i));

        if (a.getReservas().size() != b.getReservas().size()) d.add("cantidad de reservas");
        else for (int i = 0; i < a.getReservas().size(); i++) {
            Reserva x = a.getReservas().get(i), y = b.getReservas().get(i);
            if (!x.getUsuario().getCodigo().equals(y.getUsuario().getCodigo())
                    || !x.getMaterial().getCodigo().equals(y.getMaterial().getCodigo())
                    || !x.getFechaReserva().equals(y.getFechaReserva()) || x.getEstado() != y.getEstado())
                d.add("reserva " + (i + 1));
        }
        return d;
    }

    private static void comparar(List<String> d, Material x, Material y) {
        String c = x.getCodigo();
        if (!c.equals(y.getCodigo()) || !x.obtenerTipo().equals(y.obtenerTipo()) || !x.getTitulo().equals(y.getTitulo())
                || !x.getAutor().equals(y.getAutor()) || x.getAnio() != y.getAnio() || x.isDisponible() != y.isDisponible())
            d.add("material " + c);
        if (x instanceof Libro l && y instanceof Libro m && !(l.getIsbn().equals(m.getIsbn()) && l.getEditorial().equals(m.getEditorial())))
            d.add("libro " + c);
        if (x instanceof Tesis l && y instanceof Tesis m && !(l.getAsesor().equals(m.getAsesor()) && l.getGrado().equals(m.getGrado())))
            d.add("tesis " + c);
        if (x instanceof Revista l && y instanceof Revista m && !(l.getIssn().equals(m.getIssn())
                && l.getVolumen() == m.getVolumen() && l.getNumero() == m.getNumero()))
            d.add("revista " + c);
    }

    private static void comparar(List<String> d, Usuario x, Usuario y) {
        String c = x.getCodigo();
        if (!c.equals(y.getCodigo()) || !x.obtenerTipo().equals(y.obtenerTipo()) || !x.getNombre().equals(y.getNombre())
                || !x.getCorreo().equals(y.getCorreo()))
            d.add("usuario " + c);
        String dx = x instanceof Estudiante e ? e.getEscuela() : x instanceof Docente o ? o.getDepartamento() : ((Administrativo) x).getArea();
        String dy = y instanceof Estudiante e ? e.getEscuela() : y instanceof Docente o ? o.getDepartamento() : ((Administrativo) y).getArea();
        if (!dx.equals(dy)) d.add("dato propio del usuario " + c);
    }

    private static void comparar(List<String> d, int n, Prestamo x, Prestamo y) {
        if (!x.getUsuario().getCodigo().equals(y.getUsuario().getCodigo())
                || !x.getMaterial().getCodigo().equals(y.getMaterial().getCodigo())
                || !x.getFechaPrestamo().equals(y.getFechaPrestamo()) || !x.getFechaVencimiento().equals(y.getFechaVencimiento())
                || !java.util.Objects.equals(x.getFechaDevolucion(), y.getFechaDevolucion()))
            d.add("préstamo " + n);
        if (x.getEstado() != y.getEstado()) d.add("estado del préstamo " + n);
        Multa mx = x.getMulta(), my = y.getMulta();
        if ((mx == null) != (my == null)) d.add("multa del préstamo " + n);
        else if (mx != null && (mx.getDiasRetraso() != my.getDiasRetraso() || mx.getMonto() != my.getMonto()
                || mx.isPagada() != my.isPagada()))
            d.add("datos de la multa del préstamo " + n);
    }
}
