import biblioteca.modelo.*;
import biblioteca.persistencia.Mapeo;
import biblioteca.persistencia.Sql;
import biblioteca.servicio.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Valida las sentencias SQL del proyecto con un SQLite real, sin el driver JDBC:
 *   1) "exportar": escribe las sentencias de Sql y las filas de DatosEjemplo (generadas con Mapeo).
 *   2) test/validar_sqlite.py las ejecuta con SQLite y guarda lo que devuelven los SELECT.
 *   3) "importar": lee esas filas, arma una Biblioteca con Mapeo y la compara con la original.
 */
public class ValidarConSqlite {
    static final Path CARPETA = Paths.get("datos/validacion");

    static String codificar(Object v) {
        if (v == null) return "n:";
        String t = v.toString().replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n");
        if (v instanceof Integer || v instanceof Long) return "i:" + t;
        if (v instanceof Double) return "d:" + t;
        return "s:" + t;
    }

    static Object decodificar(String c) {
        String t = c.substring(2).replace("\\n", "\n").replace("\\t", "\t").replace("\\\\", "\\");
        return switch (c.charAt(0)) {
            case 'n' -> null;
            case 'i' -> Long.parseLong(t);
            case 'd' -> Double.parseDouble(t);
            default -> t;
        };
    }

    static String linea(String nombre, Object[] fila) {
        StringBuilder s = new StringBuilder(nombre);
        for (Object v : fila) s.append('\t').append(codificar(v));
        return s.toString();
    }

    static Biblioteca ejemplo() {
        Biblioteca b = DatosEjemplo.crear();
        b.pagarMultas("E002");
        b.reservarMaterial("E001", "L003");
        b.reservarMaterial("D001", "L003").cancelar();
        b.obtenerPrestamosVencidos();   // marca como VENCIDO el préstamo atrasado, como hace la interfaz al refrescar
        return b;
    }

    static void exportar() throws IOException {
        Files.createDirectories(CARPETA);
        List<String> sentencias = new ArrayList<>();
        for (String s : Sql.CREAR_TABLAS) sentencias.add("CREAR\t" + s);
        for (String s : Sql.BORRAR_TODO) sentencias.add("BORRAR\t" + s);
        sentencias.add("INSERTAR_MATERIAL\t" + Sql.INSERTAR_MATERIAL);
        sentencias.add("INSERTAR_USUARIO\t" + Sql.INSERTAR_USUARIO);
        sentencias.add("INSERTAR_PRESTAMO\t" + Sql.INSERTAR_PRESTAMO);
        sentencias.add("INSERTAR_MULTA\t" + Sql.INSERTAR_MULTA);
        sentencias.add("INSERTAR_RESERVA\t" + Sql.INSERTAR_RESERVA);
        sentencias.add("LEER_MATERIALES\t" + Sql.LEER_MATERIALES);
        sentencias.add("LEER_USUARIOS\t" + Sql.LEER_USUARIOS);
        sentencias.add("LEER_PRESTAMOS\t" + Sql.LEER_PRESTAMOS);
        sentencias.add("LEER_RESERVAS\t" + Sql.LEER_RESERVAS);
        Files.write(CARPETA.resolve("sentencias.tsv"), sentencias, StandardCharsets.UTF_8);

        Biblioteca b = ejemplo();
        List<String> filas = new ArrayList<>();
        for (Material m : b.getMateriales()) filas.add(linea("INSERTAR_MATERIAL", Mapeo.filaMaterial(m)));
        for (Usuario u : b.getUsuarios()) filas.add(linea("INSERTAR_USUARIO", Mapeo.filaUsuario(u)));
        int id = 1;
        for (Prestamo p : b.getPrestamos()) {
            filas.add(linea("INSERTAR_PRESTAMO", Mapeo.filaPrestamo(id, p)));
            Object[] multa = Mapeo.filaMulta(id, p);
            if (multa != null) filas.add(linea("INSERTAR_MULTA", multa));
            id++;
        }
        id = 1;
        for (Reserva r : b.getReservas()) filas.add(linea("INSERTAR_RESERVA", Mapeo.filaReserva(id++, r)));
        Files.write(CARPETA.resolve("filas.tsv"), filas, StandardCharsets.UTF_8);
        System.out.println("Exportadas " + sentencias.size() + " sentencias y " + filas.size() + " filas a " + CARPETA);
    }

    static List<Object[]> leer(List<String> lineas, String nombre) {
        List<Object[]> r = new ArrayList<>();
        for (String l : lineas) {
            String[] p = l.split("\t", -1);
            if (!p[0].equals(nombre)) continue;
            Object[] fila = new Object[p.length - 1];
            for (int i = 1; i < p.length; i++) fila[i - 1] = decodificar(p[i]);
            r.add(fila);
        }
        return r;
    }

    static void importar() throws IOException {
        List<String> lineas = Files.readAllLines(CARPETA.resolve("resultados.tsv"), StandardCharsets.UTF_8);
        Biblioteca n = new Biblioteca("BiblioUnsa");
        for (Object[] f : leer(lineas, "LEER_MATERIALES")) n.registrarMaterial(Mapeo.material(f));
        for (Object[] f : leer(lineas, "LEER_USUARIOS")) n.registrarUsuario(Mapeo.usuario(f));
        for (Object[] f : leer(lineas, "LEER_PRESTAMOS")) n.getPrestamos().add(Mapeo.prestamo(f, n));
        for (Object[] f : leer(lineas, "LEER_RESERVAS")) n.getReservas().add(Mapeo.reserva(f, n));

        List<String> dif = Comparador.diferencias(ejemplo(), n);
        System.out.println("Leídos desde SQLite: " + n.getMateriales().size() + " materiales, " + n.getUsuarios().size()
                + " usuarios, " + n.getPrestamos().size() + " préstamos, " + n.getReservas().size() + " reservas");
        System.out.println(dif.isEmpty() ? "RESULTADO: la biblioteca leída de SQLite es idéntica a la original"
                : "RESULTADO: DIFERENCIAS " + dif);
        System.exit(dif.isEmpty() ? 0 : 1);
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("importar")) importar(); else exportar();
    }
}
