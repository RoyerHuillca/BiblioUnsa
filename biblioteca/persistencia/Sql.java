package biblioteca.persistencia;

/**
 * Sentencias SQL de la base de datos (SQLite). Se dejan en un solo lugar para que
 * BaseDatos solo se ocupe de ejecutar y Mapeo solo de convertir objetos en filas.
 */
public final class Sql {
    private Sql() { }

    public static final String[] CREAR_TABLAS = {
        "CREATE TABLE IF NOT EXISTS material ("
            + "codigo TEXT PRIMARY KEY, tipo TEXT NOT NULL, titulo TEXT NOT NULL, autor TEXT, anio INTEGER, "
            + "disponible INTEGER NOT NULL, dato1 TEXT, dato2 TEXT, dato3 TEXT)",
        "CREATE TABLE IF NOT EXISTS usuario ("
            + "codigo TEXT PRIMARY KEY, tipo TEXT NOT NULL, nombre TEXT NOT NULL, correo TEXT, dato TEXT)",
        "CREATE TABLE IF NOT EXISTS prestamo ("
            + "id INTEGER PRIMARY KEY, usuario TEXT NOT NULL REFERENCES usuario(codigo), "
            + "material TEXT NOT NULL REFERENCES material(codigo), fecha_prestamo TEXT NOT NULL, "
            + "fecha_vencimiento TEXT NOT NULL, fecha_devolucion TEXT, estado TEXT NOT NULL)",
        "CREATE TABLE IF NOT EXISTS multa ("
            + "prestamo_id INTEGER PRIMARY KEY REFERENCES prestamo(id), dias_retraso INTEGER NOT NULL, "
            + "monto REAL NOT NULL, pagada INTEGER NOT NULL)",
        "CREATE TABLE IF NOT EXISTS reserva ("
            + "id INTEGER PRIMARY KEY, usuario TEXT NOT NULL REFERENCES usuario(codigo), "
            + "material TEXT NOT NULL REFERENCES material(codigo), fecha_reserva TEXT NOT NULL, estado TEXT NOT NULL)"
    };

    /** Se borra en este orden para respetar las claves foráneas. */
    public static final String[] BORRAR_TODO = {
        "DELETE FROM reserva", "DELETE FROM multa", "DELETE FROM prestamo", "DELETE FROM usuario", "DELETE FROM material"
    };

    public static final String INSERTAR_MATERIAL =
        "INSERT INTO material (codigo, tipo, titulo, autor, anio, disponible, dato1, dato2, dato3) VALUES (?,?,?,?,?,?,?,?,?)";
    public static final String INSERTAR_USUARIO =
        "INSERT INTO usuario (codigo, tipo, nombre, correo, dato) VALUES (?,?,?,?,?)";
    public static final String INSERTAR_PRESTAMO =
        "INSERT INTO prestamo (id, usuario, material, fecha_prestamo, fecha_vencimiento, fecha_devolucion, estado) VALUES (?,?,?,?,?,?,?)";
    public static final String INSERTAR_MULTA =
        "INSERT INTO multa (prestamo_id, dias_retraso, monto, pagada) VALUES (?,?,?,?)";
    public static final String INSERTAR_RESERVA =
        "INSERT INTO reserva (id, usuario, material, fecha_reserva, estado) VALUES (?,?,?,?,?)";

    public static final String LEER_MATERIALES =
        "SELECT codigo, tipo, titulo, autor, anio, disponible, dato1, dato2, dato3 FROM material ORDER BY rowid";
    public static final String LEER_USUARIOS =
        "SELECT codigo, tipo, nombre, correo, dato FROM usuario ORDER BY rowid";
    /** Cada préstamo con su multa (si la tiene): 7 columnas del préstamo y 3 de la multa. */
    public static final String LEER_PRESTAMOS =
        "SELECT p.id, p.usuario, p.material, p.fecha_prestamo, p.fecha_vencimiento, p.fecha_devolucion, p.estado, "
            + "m.dias_retraso, m.monto, m.pagada FROM prestamo p LEFT JOIN multa m ON m.prestamo_id = p.id ORDER BY p.id";
    public static final String LEER_RESERVAS =
        "SELECT id, usuario, material, fecha_reserva, estado FROM reserva ORDER BY id";
}
