package biblioteca.persistencia;

import biblioteca.modelo.*;
import biblioteca.servicio.Biblioteca;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y carga la biblioteca en una base de datos SQLite usando JDBC.
 * Tablas: material, usuario, prestamo, multa y reserva.
 * Necesita el driver sqlite-jdbc en la carpeta lib (ver README).
 */
public class BaseDatos {
    public static final String DRIVER = "org.sqlite.JDBC";

    private final String ruta;

    public BaseDatos(String ruta) { this.ruta = ruta; }

    public String getRuta() { return ruta; }

    /** Indica si el driver de SQLite está disponible en el classpath. */
    public static boolean hayDriver() {
        try {
            Class.forName(DRIVER);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private Connection conectar() throws SQLException {
        if (!hayDriver())
            throw new SQLException("Falta el driver sqlite-jdbc. Descargue sqlite-jdbc-3.36.0.3.jar, póngalo en la carpeta lib y ejecute con ese jar en el classpath.");
        try {
            Path padre = Paths.get(ruta).toAbsolutePath().getParent();
            if (padre != null) Files.createDirectories(padre);
        } catch (java.io.IOException e) {
            throw new SQLException("No se pudo crear la carpeta de la base de datos: " + e.getMessage(), e);
        }
        return DriverManager.getConnection("jdbc:sqlite:" + ruta);
    }

    private static void crearTablas(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            for (String sql : Sql.CREAR_TABLAS) st.execute(sql);
        }
    }

    /** Reemplaza todo el contenido de la base de datos por el de la biblioteca (en una sola transacción). */
    public void guardar(Biblioteca b) throws SQLException {
        try (Connection c = conectar()) {
            c.setAutoCommit(false);
            try {
                crearTablas(c);
                try (Statement st = c.createStatement()) {
                    for (String sql : Sql.BORRAR_TODO) st.executeUpdate(sql);
                }
                try (PreparedStatement ps = c.prepareStatement(Sql.INSERTAR_MATERIAL)) {
                    for (Material m : b.getMateriales()) insertar(ps, Mapeo.filaMaterial(m));
                }
                try (PreparedStatement ps = c.prepareStatement(Sql.INSERTAR_USUARIO)) {
                    for (Usuario u : b.getUsuarios()) insertar(ps, Mapeo.filaUsuario(u));
                }
                try (PreparedStatement pp = c.prepareStatement(Sql.INSERTAR_PRESTAMO);
                     PreparedStatement pm = c.prepareStatement(Sql.INSERTAR_MULTA)) {
                    int id = 1;
                    for (Prestamo p : b.getPrestamos()) {
                        insertar(pp, Mapeo.filaPrestamo(id, p));
                        Object[] multa = Mapeo.filaMulta(id, p);
                        if (multa != null) insertar(pm, multa);
                        id++;
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(Sql.INSERTAR_RESERVA)) {
                    int id = 1;
                    for (Reserva r : b.getReservas()) insertar(ps, Mapeo.filaReserva(id++, r));
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Lee la base de datos y arma una Biblioteca nueva. */
    public Biblioteca cargar() throws SQLException {
        try (Connection c = conectar()) {
            crearTablas(c);
            Biblioteca b = new Biblioteca("BiblioUnsa");
            List<Object[]> materiales = leer(c, Sql.LEER_MATERIALES, 9);
            List<Object[]> usuarios = leer(c, Sql.LEER_USUARIOS, 5);
            if (materiales.isEmpty() && usuarios.isEmpty())
                throw new SQLException("La base de datos está vacía. Primero use \"Guardar en base de datos\".");
            for (Object[] f : materiales) b.registrarMaterial(Mapeo.material(f));
            for (Object[] f : usuarios) b.registrarUsuario(Mapeo.usuario(f));
            for (Object[] f : leer(c, Sql.LEER_PRESTAMOS, 10)) b.getPrestamos().add(Mapeo.prestamo(f, b));
            for (Object[] f : leer(c, Sql.LEER_RESERVAS, 5)) b.getReservas().add(Mapeo.reserva(f, b));
            return b;
        }
    }

    private static void insertar(PreparedStatement ps, Object[] fila) throws SQLException {
        for (int i = 0; i < fila.length; i++) ps.setObject(i + 1, fila[i]);
        ps.executeUpdate();
    }

    private static List<Object[]> leer(Connection c, String sql, int columnas) throws SQLException {
        List<Object[]> filas = new ArrayList<>();
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Object[] fila = new Object[columnas];
                for (int i = 0; i < columnas; i++) fila[i] = rs.getObject(i + 1);
                filas.add(fila);
            }
        }
        return filas;
    }
}
