package biblioteca.pruebas;

import biblioteca.persistencia.BaseDatos;
import biblioteca.servicio.*;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * Pruebas de la base de datos real (JDBC + SQLite). Necesitan el driver en el classpath:
 *   java -cp "out;lib/sqlite-jdbc-3.36.0.3.jar" biblioteca.pruebas.PruebasBaseDatos
 */
public class PruebasBaseDatos {
    static int ok = 0, fallos = 0;

    static void verificar(String nombre, boolean condicion) {
        if (condicion) { ok++; System.out.println("  OK    " + nombre); }
        else { fallos++; System.out.println("  FALLO " + nombre); }
    }

    public static void main(String[] args) throws Exception {
        if (!BaseDatos.hayDriver()) {
            System.out.println("PRUEBA OMITIDA: falta el driver sqlite-jdbc en el classpath.");
            System.out.println("Ponga lib/sqlite-jdbc-3.36.0.3.jar en el classpath y vuelva a ejecutar.");
            System.exit(2);
        }
        File archivo = new File("datos/prueba_bd.db");
        archivo.delete();
        BaseDatos bd = new BaseDatos(archivo.getPath());

        System.out.println("Base de datos vacía");
        boolean error = false;
        try { bd.cargar(); } catch (SQLException e) { error = e.getMessage().contains("vacía"); }
        verificar("cargar una base vacía avisa que está vacía", error);

        System.out.println("Guardar y cargar");
        Biblioteca original = DatosEjemplo.crear();
        bd.guardar(original);
        verificar("se crea el archivo .db", archivo.exists() && archivo.length() > 0);
        Biblioteca cargada = bd.cargar();
        List<String> dif = Comparador.diferencias(original, cargada);
        verificar("lo cargado es igual a lo guardado" + (dif.isEmpty() ? "" : " " + dif), dif.isEmpty());
        verificar("la deuda de E002 se conserva (S/ 13.00)", cargada.deudaDe(cargada.buscarUsuario("E002")) == 13.0);

        System.out.println("Guardar de nuevo con cambios");
        original.pagarMultas("E002");
        original.reservarMaterial("E001", "L003");
        bd.guardar(original);
        bd.guardar(original);
        cargada = bd.cargar();
        dif = Comparador.diferencias(original, cargada);
        verificar("guardar dos veces no duplica filas" + (dif.isEmpty() ? "" : " " + dif), dif.isEmpty());
        verificar("multa pagada y reserva pendiente se conservan", cargada.deudaDe(cargada.buscarUsuario("E002")) == 0
                && cargada.reservasPendientes().size() == 1);

        archivo.delete();
        System.out.println("\nResultado: " + ok + " correctas, " + fallos + " fallos");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
