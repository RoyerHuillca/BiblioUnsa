package biblioteca.persistencia;

import biblioteca.modelo.*;
import biblioteca.servicio.Biblioteca;

import java.time.LocalDate;

/**
 * Convierte objetos del modelo en filas de la base de datos (Object[]) y viceversa.
 * No usa JDBC, por eso se puede probar sin el driver.
 */
public final class Mapeo {
    private Mapeo() { }

    // ----------------------------- Material -----------------------------
    public static Object[] filaMaterial(Material m) {
        String d1 = null, d2 = null, d3 = null;
        if (m instanceof Libro l) { d1 = l.getIsbn(); d2 = l.getEditorial(); }
        else if (m instanceof Tesis t) { d1 = t.getAsesor(); d2 = t.getGrado(); }
        else if (m instanceof Revista r) { d1 = r.getIssn(); d2 = String.valueOf(r.getVolumen()); d3 = String.valueOf(r.getNumero()); }
        return new Object[]{m.getCodigo(), m.obtenerTipo(), m.getTitulo(), m.getAutor(), m.getAnio(),
                m.isDisponible() ? 1 : 0, d1, d2, d3};
    }

    public static Material material(Object[] f) {
        String codigo = texto(f[0]), tipo = texto(f[1]), titulo = texto(f[2]), autor = texto(f[3]);
        int anio = entero(f[4]);
        Material m = switch (tipo) {
            case "Libro" -> new Libro(codigo, titulo, autor, anio, texto(f[6]), texto(f[7]));
            case "Tesis" -> new Tesis(codigo, titulo, autor, anio, texto(f[6]), texto(f[7]));
            case "Revista" -> new Revista(codigo, titulo, autor, anio, texto(f[6]), entero(f[7]), entero(f[8]));
            default -> throw new IllegalArgumentException("Tipo de material desconocido: " + tipo);
        };
        m.setDisponible(entero(f[5]) == 1);
        return m;
    }

    // ----------------------------- Usuario -----------------------------
    public static Object[] filaUsuario(Usuario u) {
        String dato = null;
        if (u instanceof Estudiante e) dato = e.getEscuela();
        else if (u instanceof Docente d) dato = d.getDepartamento();
        else if (u instanceof Administrativo a) dato = a.getArea();
        return new Object[]{u.getCodigo(), u.obtenerTipo(), u.getNombre(), u.getCorreo(), dato};
    }

    public static Usuario usuario(Object[] f) {
        String codigo = texto(f[0]), tipo = texto(f[1]), nombre = texto(f[2]), correo = texto(f[3]), dato = texto(f[4]);
        return switch (tipo) {
            case "Estudiante" -> new Estudiante(codigo, nombre, correo, dato);
            case "Docente" -> new Docente(codigo, nombre, correo, dato);
            case "Administrativo" -> new Administrativo(codigo, nombre, correo, dato);
            default -> throw new IllegalArgumentException("Tipo de usuario desconocido: " + tipo);
        };
    }

    // ----------------------------- Préstamo y multa -----------------------------
    public static Object[] filaPrestamo(int id, Prestamo p) {
        return new Object[]{id, p.getUsuario().getCodigo(), p.getMaterial().getCodigo(),
                p.getFechaPrestamo().toString(), p.getFechaVencimiento().toString(),
                p.getFechaDevolucion() == null ? null : p.getFechaDevolucion().toString(), p.getEstado().name()};
    }

    /** Devuelve la fila de la multa del préstamo, o null si no tiene. */
    public static Object[] filaMulta(int idPrestamo, Prestamo p) {
        Multa m = p.getMulta();
        if (m == null) return null;
        return new Object[]{idPrestamo, m.getDiasRetraso(), m.getMonto(), m.isPagada() ? 1 : 0};
    }

    /** Fila de 10 columnas (LEER_PRESTAMOS): 7 del préstamo y 3 de la multa (pueden ser null). */
    public static Prestamo prestamo(Object[] f, Biblioteca b) {
        Usuario u = b.buscarUsuario(texto(f[1]));
        Material m = b.buscarMaterialPorCodigo(texto(f[2]));
        if (u == null || m == null) throw new IllegalArgumentException("El préstamo " + f[0] + " usa un usuario o material que no existe.");
        Prestamo p = new Prestamo(u, m, LocalDate.parse(texto(f[3])), LocalDate.parse(texto(f[4])));
        if (f[5] != null) {
            p.registrarDevolucion(LocalDate.parse(texto(f[5])));   // vuelve a generar la multa si hubo retraso
            if (p.getMulta() != null && f[9] != null && entero(f[9]) == 1) p.getMulta().pagar();
        }
        return p;
    }

    // ----------------------------- Reserva -----------------------------
    public static Object[] filaReserva(int id, Reserva r) {
        return new Object[]{id, r.getUsuario().getCodigo(), r.getMaterial().getCodigo(),
                r.getFechaReserva().toString(), r.getEstado().name()};
    }

    public static Reserva reserva(Object[] f, Biblioteca b) {
        Usuario u = b.buscarUsuario(texto(f[1]));
        Material m = b.buscarMaterialPorCodigo(texto(f[2]));
        if (u == null || m == null) throw new IllegalArgumentException("La reserva " + f[0] + " usa un usuario o material que no existe.");
        Reserva r = new Reserva(u, m, LocalDate.parse(texto(f[3])));
        switch (EstadoReserva.valueOf(texto(f[4]))) {
            case ATENDIDA -> r.atender();
            case CANCELADA -> r.cancelar();
            default -> { }
        }
        return r;
    }

    // ----------------------------- Conversión de valores -----------------------------
    static String texto(Object v) { return v == null ? null : v.toString(); }

    /** SQLite devuelve los enteros como Integer o Long y a veces como texto; aquí se aceptan todos. */
    static int entero(Object v) {
        if (v instanceof Number n) return n.intValue();
        return Integer.parseInt(v.toString().trim());
    }
}
