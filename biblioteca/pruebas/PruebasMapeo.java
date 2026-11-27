package biblioteca.pruebas;

import biblioteca.modelo.*;
import biblioteca.persistencia.Mapeo;
import biblioteca.servicio.*;

import java.util.List;

/** Pruebas de la conversión objetos <-> filas de la base de datos. No necesitan el driver de SQLite. */
public class PruebasMapeo {
    static int ok = 0, fallos = 0;

    static void verificar(String nombre, boolean condicion) {
        if (condicion) { ok++; System.out.println("  OK    " + nombre); }
        else { fallos++; System.out.println("  FALLO " + nombre); }
    }

    /** SQLite devuelve los enteros como Long; se imita para probar la conversión. */
    static Object[] comoSqlite(Object[] fila) {
        Object[] r = fila.clone();
        for (int i = 0; i < r.length; i++) if (r[i] instanceof Integer n) r[i] = n.longValue();
        return r;
    }

    static Biblioteca reconstruir(Biblioteca b) {
        Biblioteca n = new Biblioteca("BiblioUnsa");
        for (Material m : b.getMateriales()) n.registrarMaterial(Mapeo.material(comoSqlite(Mapeo.filaMaterial(m))));
        for (Usuario u : b.getUsuarios()) n.registrarUsuario(Mapeo.usuario(comoSqlite(Mapeo.filaUsuario(u))));
        int id = 1;
        for (Prestamo p : b.getPrestamos()) {
            Object[] f = Mapeo.filaPrestamo(id, p), mu = Mapeo.filaMulta(id, p);
            Object[] fila = new Object[10];
            System.arraycopy(f, 0, fila, 0, 7);
            if (mu != null) { fila[7] = mu[1]; fila[8] = mu[2]; fila[9] = mu[3]; }
            n.getPrestamos().add(Mapeo.prestamo(comoSqlite(fila), n));
            id++;
        }
        id = 1;
        for (Reserva r : b.getReservas()) n.getReservas().add(Mapeo.reserva(comoSqlite(Mapeo.filaReserva(id++, r)), n));
        return n;
    }

    public static void main(String[] args) {
        Biblioteca b = DatosEjemplo.crear();

        System.out.println("Filas de cada tabla");
        Object[] libro = Mapeo.filaMaterial(b.buscarMaterialPorCodigo("L001"));
        verificar("libro: tipo, año y disponible", "Libro".equals(libro[1]) && (int) libro[4] == 2016 && (int) libro[5] == 1);
        verificar("libro: ISBN y editorial en dato1 y dato2", "978-0134743356".equals(libro[6]) && "Pearson".equals(libro[7]) && libro[8] == null);
        Object[] revista = Mapeo.filaMaterial(b.buscarMaterialPorCodigo("R001"));
        verificar("revista: ISSN, volumen y número", "1234-5678".equals(revista[6]) && "12".equals(revista[7]) && "3".equals(revista[8]));
        verificar("material prestado: disponible = 0", (int) Mapeo.filaMaterial(b.buscarMaterialPorCodigo("L003"))[5] == 0);
        Object[] docente = Mapeo.filaUsuario(b.buscarUsuario("D001"));
        verificar("docente: tipo y departamento", "Docente".equals(docente[1]) && "Ingeniería de Sistemas".equals(docente[4]));
        Prestamo conMulta = b.getPrestamos().get(0);
        verificar("préstamo devuelto con multa: fecha de devolución y estado",
                Mapeo.filaPrestamo(1, conMulta)[5] != null && "DEVUELTO".equals(Mapeo.filaPrestamo(1, conMulta)[6]));
        Object[] multa = Mapeo.filaMulta(1, conMulta);
        verificar("multa: 13 días, S/ 13.00, no pagada", multa != null && (int) multa[1] == 13 && (double) multa[2] == 13.0 && (int) multa[3] == 0);
        verificar("préstamo sin multa no genera fila de multa", Mapeo.filaMulta(4, b.getPrestamos().get(3)) == null);

        System.out.println("Ida y vuelta (objetos -> filas -> objetos)");
        List<String> dif = Comparador.diferencias(b, reconstruir(b));
        verificar("los datos de ejemplo se reconstruyen idénticos" + (dif.isEmpty() ? "" : " " + dif), dif.isEmpty());
        Biblioteca c = reconstruir(b);
        verificar("la deuda de E002 sigue siendo S/ 13.00", c.deudaDe(c.buscarUsuario("E002")) == 13.0);
        verificar("L003 sigue prestado y vencido", !c.buscarMaterialPorCodigo("L003").isDisponible() && c.obtenerPrestamosVencidos().size() == 1);

        b.pagarMultas("E002");
        b.reservarMaterial("E001", "L003");
        b.reservarMaterial("D001", "L003").cancelar();
        Biblioteca d = reconstruir(b);
        dif = Comparador.diferencias(b, d);
        verificar("multa pagada y reservas PENDIENTE/CANCELADA se conservan" + (dif.isEmpty() ? "" : " " + dif), dif.isEmpty());
        verificar("la multa pagada ya no cuenta como deuda", d.deudaDe(d.buscarUsuario("E002")) == 0);
        verificar("1 reserva pendiente y 1 cancelada", d.reservasPendientes().size() == 1 && d.getReservas().size() == 2);

        System.out.println("Casos de error");
        boolean rechazado = false;
        try { Mapeo.material(new Object[]{"X1", "Disco", "t", "a", 2000L, 1L, null, null, null}); }
        catch (IllegalArgumentException e) { rechazado = true; }
        verificar("tipo de material desconocido se rechaza", rechazado);
        rechazado = false;
        try { Mapeo.prestamo(new Object[]{1L, "NOEXISTE", "L001", "2026-01-01", "2026-01-08", null, "ACTIVO", null, null, null}, b); }
        catch (IllegalArgumentException e) { rechazado = true; }
        verificar("préstamo de un usuario inexistente se rechaza", rechazado);

        System.out.println("\nResultado: " + ok + " correctas, " + fallos + " fallos");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
