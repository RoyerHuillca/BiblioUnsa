import biblioteca.modelo.*;
import biblioteca.persistencia.Respaldo;
import biblioteca.servicio.*;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

/** Pruebas de la lógica de negocio (sin JUnit): ejecutar con ./probar.sh */
public class PruebasLogica {
    static int ok = 0, fallos = 0;

    static void verificar(String nombre, boolean condicion) {
        if (condicion) { ok++; System.out.println("  OK    " + nombre); }
        else { fallos++; System.out.println("  FALLO " + nombre); }
    }

    /** true si la operación fue rechazada con BibliotecaException. */
    static boolean rechaza(Runnable r) {
        try { r.run(); return false; } catch (BibliotecaException e) { return true; }
    }

    public static void main(String[] args) throws Exception {
        Biblioteca b = DatosEjemplo.crear();
        LocalDate hoy = LocalDate.now();
        Reporte reporte = new Reporte(b);

        System.out.println("Polimorfismo (plazo y límite por tipo de usuario)");
        verificar("estudiante: 7 días y 3 préstamos", b.buscarUsuario("E001").obtenerPlazoPrestamo() == 7
                && b.buscarUsuario("E001").obtenerLimitePrestamos() == 3);
        verificar("docente: 15 días y 5 préstamos", b.buscarUsuario("D001").obtenerPlazoPrestamo() == 15
                && b.buscarUsuario("D001").obtenerLimitePrestamos() == 5);
        verificar("administrativo: 10 días y 4 préstamos", b.buscarUsuario("A001").obtenerPlazoPrestamo() == 10
                && b.buscarUsuario("A001").obtenerLimitePrestamos() == 4);
        verificar("puedePrestar respeta el límite", b.buscarUsuario("E001").puedePrestar(2)
                && !b.buscarUsuario("E001").puedePrestar(3));
        verificar("tipos de material", b.buscarMaterialPorCodigo("L001").obtenerTipo().equals("Libro")
                && b.buscarMaterialPorCodigo("T001").obtenerTipo().equals("Tesis")
                && b.buscarMaterialPorCodigo("R001").obtenerTipo().equals("Revista"));

        System.out.println("Datos de ejemplo");
        verificar("9 materiales y 4 usuarios", b.getMateriales().size() == 9 && b.getUsuarios().size() == 4);
        verificar("E002 debe S/ 13.00 (13 días x S/ 1.00)", Math.abs(b.deudaDe(b.buscarUsuario("E002")) - 13.0) < 1e-9);
        verificar("1 préstamo vencido (L003)", b.obtenerPrestamosVencidos().size() == 1
                && b.obtenerPrestamosVencidos().get(0).getMaterial().getCodigo().equals("L003"));
        verificar("estado VENCIDO asignado", b.obtenerPrestamosVencidos().get(0).getEstado() == EstadoPrestamo.VENCIDO);

        System.out.println("Materiales");
        verificar("buscar por título 'java'", b.buscarMaterial("java").size() == 1);
        verificar("buscar por autor sin tilde 'garcia'", b.buscarMaterial("garcia").size() == 1);
        verificar("buscar por código 'T00'", b.buscarMaterial("T00").size() == 2);
        verificar("código repetido rechazado", rechaza(() -> b.registrarMaterial(new Libro("L001", "X", "Y", 2000, "1", "Z"))));
        verificar("código vacío rechazado", rechaza(() -> b.registrarMaterial(new Libro("", "X", "Y", 2000, "1", "Z"))));
        b.registrarMaterial(new Libro("L099", "Prueba", "Autor", 2020, "000", "Ed"));
        verificar("editar material", b.editarMaterial("L099", "Prueba 2", "Autor 2")
                && b.buscarMaterialPorCodigo("L099").getTitulo().equals("Prueba 2"));
        verificar("editar material inexistente = false", !b.editarMaterial("ZZZ", "a", "b"));
        verificar("no se elimina material prestado", !b.eliminarMaterial("L003"));
        verificar("se elimina material disponible", b.eliminarMaterial("L099") && b.buscarMaterialPorCodigo("L099") == null);

        System.out.println("Préstamos y multas");
        verificar("usuario con multa no puede prestar", rechaza(() -> b.registrarPrestamo("E002", "L004")));
        verificar("material prestado no se presta", rechaza(() -> b.registrarPrestamo("E001", "L003")));
        verificar("usuario inexistente", rechaza(() -> b.registrarPrestamo("X999", "L004")));
        Prestamo p = b.registrarPrestamo("E001", "L004");
        verificar("vence a los 7 días (estudiante)", p.getFechaVencimiento().equals(hoy.plusDays(7)));
        verificar("préstamo ACTIVO y material no disponible", p.getEstado() == EstadoPrestamo.ACTIVO
                && !b.buscarMaterialPorCodigo("L004").isDisponible());
        b.registrarPrestamo("E001", "L005");          // E001 ya tenía R001 -> 3 préstamos
        verificar("límite de 3 para estudiante", rechaza(() -> b.registrarPrestamo("E001", "T001")));
        verificar("devolución a tiempo sin multa", b.registrarDevolucion("L004") == null);
        verificar("devolver material no prestado falla", rechaza(() -> b.registrarDevolucion("L004")));
        Multa m = b.registrarDevolucion("L003");      // L003 estaba vencido hace 10 días
        verificar("devolución tardía: 10 días = S/ 10.00", m != null && m.getDiasRetraso() == 10 && m.getMonto() == 10.0);
        verificar("deuda del administrativo = S/ 10.00", Math.abs(b.deudaDe(b.buscarUsuario("A001")) - 10.0) < 1e-9);
        verificar("pagar multas deja la deuda en 0", b.pagarMultas("A001") == 10.0 && b.deudaDe(b.buscarUsuario("A001")) == 0);

        System.out.println("Reservas");
        b.registrarPrestamo("D001", "T001");
        Reserva r = b.reservarMaterial("E001", "T001");
        verificar("reserva queda PENDIENTE", r.getEstado() == EstadoReserva.PENDIENTE);
        verificar("no se reserva dos veces", rechaza(() -> b.reservarMaterial("E001", "T001")));
        verificar("no se reserva un material disponible", rechaza(() -> b.reservarMaterial("E001", "L004")));
        b.registrarDevolucion("T001");
        verificar("reservado: otro usuario no puede prestar", rechaza(() -> b.registrarPrestamo("D001", "T001")));
        b.registrarDevolucion("L005");                // E001 libera un cupo
        b.registrarPrestamo("E001", "T001");
        verificar("quien reservó presta y la reserva queda ATENDIDA", r.getEstado() == EstadoReserva.ATENDIDA);
        Reserva r2 = b.reservarMaterial("E002", "T001");
        r2.cancelar();
        verificar("cancelar reserva", r2.getEstado() == EstadoReserva.CANCELADA && b.reservasPendientes().isEmpty());

        System.out.println("Reportes");
        verificar("más prestados ordenado de mayor a menor", reporte.librosMasPrestados().size() > 0
                && reporte.cantidadPrestamos(reporte.librosMasPrestados().get(0))
                >= reporte.cantidadPrestamos(reporte.librosMasPrestados().get(reporte.librosMasPrestados().size() - 1)));
        verificar("usuarios con deuda: E002", reporte.usuariosConDeuda().size() == 1
                && reporte.usuariosConDeuda().get(0).getCodigo().equals("E002"));
        verificar("texto del reporte con las 3 secciones", reporte.generarTexto().contains("Materiales más prestados")
                && reporte.generarTexto().contains("Usuarios con deuda") && reporte.generarTexto().contains("Préstamos vencidos"));
        reporte.exportarReporteTXT("datos/reporte_prueba.txt");
        verificar("exporta el archivo .txt", new File("datos/reporte_prueba.txt").length() > 0);
        new File("datos/reporte_prueba.txt").delete();

        System.out.println("Copia de seguridad (serialización)");
        Respaldo.guardar(b, "datos/prueba.dat");
        Biblioteca copia = Respaldo.cargar("datos/prueba.dat");
        verificar("restaura materiales, usuarios y préstamos", copia.getMateriales().size() == b.getMateriales().size()
                && copia.getUsuarios().size() == b.getUsuarios().size() && copia.getPrestamos().size() == b.getPrestamos().size());
        verificar("restaura la deuda de E002", Math.abs(copia.deudaDe(copia.buscarUsuario("E002")) - 13.0) < 1e-9);
        new File("datos/prueba.dat").delete();

        System.out.println("\nResultado: " + ok + " correctas, " + fallos + " fallos");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
