package biblioteca.consola;

import biblioteca.modelo.*;
import biblioteca.persistencia.Respaldo;
import biblioteca.servicio.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

//BiblioUnsa: sistema de gestión de biblioteca universitaria en modo consola
public class AppConsola {
    private static final String ARCHIVO_DATOS = "datos/biblioteca.dat";
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Scanner in = new Scanner(System.in, "UTF-8");
    private Biblioteca biblioteca;

    public static void main(String[] args) {
        new AppConsola().ejecutar();
    }

    private void ejecutar() {
        biblioteca = cargarInicial();
        try {
            boolean seguir = true;
            while (seguir) {
                System.out.println();
                System.out.println("===== " + biblioteca.getNombre().toUpperCase() + " - BIBLIOTECA UNIVERSITARIA =====");
                System.out.println("1. Materiales (libros, tesis y revistas)");
                System.out.println("2. Usuarios");
                System.out.println("3. Préstamos, devoluciones y reservas");
                System.out.println("4. Reportes");
                System.out.println("5. Copias de seguridad");
                System.out.println("0. Salir");
                switch (leerEntero("Opción: ")) {
                    case 1 -> menuMateriales();
                    case 2 -> menuUsuarios();
                    case 3 -> menuPrestamos();
                    case 4 -> menuReportes();
                    case 5 -> menuRespaldo();
                    case 0 -> seguir = false;
                    default -> System.out.println("Opción no válida.");
                }
            }
        } catch (NoSuchElementException fin) {
            System.out.println("\n(Fin de la entrada)");
        }
        try {
            Respaldo.guardar(biblioteca, ARCHIVO_DATOS);
            System.out.println("Datos guardados en " + ARCHIVO_DATOS + ". Hasta pronto.");
        } catch (Exception e) {
            System.out.println("No se pudo guardar: " + e.getMessage());
        }
    }

    private Biblioteca cargarInicial() {
        if (Respaldo.existe(ARCHIVO_DATOS)) {
            try {
                return Respaldo.cargar(ARCHIVO_DATOS);
            } catch (Exception e) {
                System.out.println("No se pudo leer los datos guardados; se usan datos de ejemplo. (" + e.getMessage() + ")");
            }
        }
        return DatosEjemplo.crear();
    }

    // MATERIALES
    private void menuMateriales() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- MATERIALES ---");
            System.out.println("1. Listar   2. Buscar (título, autor o código)   3. Registrar");
            System.out.println("4. Editar   5. Dar de baja                       0. Volver");
            switch (leerEntero("Opción: ")) {
                case 1 -> mostrarMateriales(biblioteca.getMateriales());
                case 2 -> mostrarMateriales(biblioteca.buscarMaterial(leerTexto("Buscar: ")));
                case 3 -> accion(this::registrarMaterial);
                case 4 -> {
                    String cod = leerTexto("Código del material: ");
                    String titulo = leerTexto("Nuevo título (vacío = no cambiar): ");
                    String autor = leerTexto("Nuevo autor (vacío = no cambiar): ");
                    System.out.println(biblioteca.editarMaterial(cod, titulo, autor)
                            ? "Material actualizado." : "No existe un material con ese código.");
                }
                case 5 -> System.out.println(biblioteca.eliminarMaterial(leerTexto("Código del material: "))
                        ? "Material dado de baja." : "No se pudo dar de baja: no existe o está prestado.");
                case 0 -> volver = true;
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    private void mostrarMateriales(List<Material> lista) {
        if (lista.isEmpty()) { System.out.println("No se encontraron materiales."); return; }
        System.out.printf("%-6s %-8s %-38s %-24s %-5s %-11s%n", "Cód.", "Tipo", "Título", "Autor", "Año", "Estado");
        for (Material m : lista)
            System.out.printf("%-6s %-8s %-38s %-24s %-5d %-11s%n", m.getCodigo(), m.obtenerTipo(), cortar(m.getTitulo(), 38),
                    cortar(m.getAutor(), 24), m.getAnio(), m.isDisponible() ? "Disponible" : "Prestado");
    }

    private void registrarMaterial() {
        System.out.println("Tipo: 1. Libro  2. Tesis  3. Revista");
        int tipo = leerEntero("Tipo: ");
        if (tipo < 1 || tipo > 3) { System.out.println("Tipo no válido."); return; }
        String codigo = leerTexto("Código: "), titulo = leerTexto("Título: "), autor = leerTexto("Autor: ");
        int anio = leerEntero("Año: ");
        Material m;
        if (tipo == 1) m = new Libro(codigo, titulo, autor, anio, leerTexto("ISBN: "), leerTexto("Editorial: "));
        else if (tipo == 2) m = new Tesis(codigo, titulo, autor, anio, leerTexto("Asesor: "), leerTexto("Grado: "));
        else m = new Revista(codigo, titulo, autor, anio, leerTexto("ISSN: "), leerEntero("Volumen: "), leerEntero("Número: "));
        biblioteca.registrarMaterial(m);
        System.out.println("Material registrado.");
    }

    // USUARIOS
    private void menuUsuarios() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- USUARIOS ---");
            System.out.println("1. Listar   2. Buscar por código   3. Registrar   4. Pagar multas   0. Volver");
            switch (leerEntero("Opción: ")) {
                case 1 -> listarUsuarios();
                case 2 -> {
                    Usuario u = biblioteca.buscarUsuario(leerTexto("Código: "));
                    if (u == null) System.out.println("No existe ese usuario.");
                    else System.out.printf("%s - %s (%s) | plazo %d días | límite %d | préstamos activos %d | deuda S/ %.2f%n",
                            u.getCodigo(), u.getNombre(), u.obtenerTipo(), u.obtenerPlazoPrestamo(), u.obtenerLimitePrestamos(),
                            biblioteca.prestamosActivosDe(u).size(), biblioteca.deudaDe(u));
                }
                case 3 -> accion(this::registrarUsuario);
                case 4 -> accion(() -> System.out.printf("Pago registrado: S/ %.2f%n",
                        biblioteca.pagarMultas(leerTexto("Código del usuario: "))));
                case 0 -> volver = true;
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    private void listarUsuarios() {
        System.out.printf("%-6s %-15s %-26s %-9s %-7s %-8s%n", "Cód.", "Tipo", "Nombre", "Préstamos", "Límite", "Deuda");
        for (Usuario u : biblioteca.getUsuarios())
            System.out.printf("%-6s %-15s %-26s %-9d %-7d S/ %.2f%n", u.getCodigo(), u.obtenerTipo(), cortar(u.getNombre(), 26),
                    biblioteca.prestamosActivosDe(u).size(), u.obtenerLimitePrestamos(), biblioteca.deudaDe(u));
    }

    private void registrarUsuario() {
        System.out.println("Tipo: 1. Estudiante  2. Docente  3. Administrativo");
        int tipo = leerEntero("Tipo: ");
        if (tipo < 1 || tipo > 3) { System.out.println("Tipo no válido."); return; }
        String codigo = leerTexto("Código: "), nombre = leerTexto("Nombre completo: "), correo = leerTexto("Correo: ");
        Usuario u;
        if (tipo == 1) u = new Estudiante(codigo, nombre, correo, leerTexto("Escuela: "));
        else if (tipo == 2) u = new Docente(codigo, nombre, correo, leerTexto("Departamento: "));
        else u = new Administrativo(codigo, nombre, correo, leerTexto("Área: "));
        biblioteca.registrarUsuario(u);
        System.out.println("Usuario registrado.");
    }

    // PRÉSTAMOS
    private void menuPrestamos() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- PRÉSTAMOS Y RESERVAS ---");
            System.out.println("1. Registrar préstamo   2. Registrar devolución   3. Reservar material");
            System.out.println("4. Ver préstamos        5. Ver reservas          6. Cancelar reserva   0. Volver");
            switch (leerEntero("Opción: ")) {
                case 1 -> accion(() -> {
                    Prestamo p = biblioteca.registrarPrestamo(leerTexto("Código del usuario: "), leerTexto("Código del material: "));
                    System.out.println("Préstamo registrado. Devolver hasta el " + p.getFechaVencimiento().format(FECHA) + ".");
                });
                case 2 -> accion(() -> {
                    Multa m = biblioteca.registrarDevolucion(leerTexto("Código del material: "));
                    if (m == null) System.out.println("Devolución a tiempo. Sin multa.");
                    else System.out.printf("Devolución con %d día(s) de retraso. Multa: S/ %.2f%n", m.getDiasRetraso(), m.getMonto());
                });
                case 3 -> accion(() -> {
                    biblioteca.reservarMaterial(leerTexto("Código del usuario: "), leerTexto("Código del material: "));
                    System.out.println("Reserva registrada.");
                });
                case 4 -> listarPrestamos();
                case 5 -> listarReservas();
                case 6 -> cancelarReserva();
                case 0 -> volver = true;
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    private void listarPrestamos() {
        if (biblioteca.getPrestamos().isEmpty()) { System.out.println("No hay préstamos."); return; }
        System.out.printf("%-4s %-22s %-30s %-11s %-11s %-9s %s%n", "N.º", "Usuario", "Material", "Vence", "Devuelto", "Estado", "Multa");
        int n = 1;
        for (Prestamo p : biblioteca.getPrestamos()) {
            p.estaVencido(biblioteca.hoy());   // actualiza el estado a VENCIDO si corresponde
            Multa m = p.getMulta();
            String multa = m == null ? "-" : String.format("S/ %.2f (%s)", m.getMonto(), m.isPagada() ? "pagada" : "pendiente");
            System.out.printf("%-4d %-22s %-30s %-11s %-11s %-9s %s%n", n++, cortar(p.getUsuario().getNombre(), 22),
                    cortar(p.getMaterial().getTitulo(), 30), p.getFechaVencimiento().format(FECHA),
                    p.getFechaDevolucion() == null ? "-" : p.getFechaDevolucion().format(FECHA), p.getEstado(), multa);
        }
    }

    private void listarReservas() {
        List<Reserva> pendientes = biblioteca.reservasPendientes();
        if (pendientes.isEmpty()) { System.out.println("No hay reservas pendientes."); return; }
        for (int i = 0; i < pendientes.size(); i++) {
            Reserva r = pendientes.get(i);
            System.out.printf("%d. %s reservó \"%s\" el %s%n", i + 1, r.getUsuario().getNombre(),
                    r.getMaterial().getTitulo(), r.getFechaReserva().format(FECHA));
        }
    }

    private void cancelarReserva() {
        List<Reserva> pendientes = biblioteca.reservasPendientes();
        listarReservas();
        if (pendientes.isEmpty()) return;
        int n = leerEntero("N.º de reserva a cancelar: ");
        if (n < 1 || n > pendientes.size()) { System.out.println("Número no válido."); return; }
        pendientes.get(n - 1).cancelar();
        System.out.println("Reserva cancelada.");
    }

    // REPORTES
    private void menuReportes() {
        Reporte reporte = new Reporte(biblioteca);
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- REPORTES ---");
            System.out.println("1. Libros más prestados   2. Usuarios con deuda   3. Préstamos vencidos");
            System.out.println("4. Exportar reporte a .txt   0. Volver");
            switch (leerEntero("Opción: ")) {
                case 1 -> {
                    List<Material> top = reporte.librosMasPrestados();
                    if (top.isEmpty()) System.out.println("Aún no hay préstamos.");
                    int pos = 1;
                    for (Material m : top)
                        System.out.printf("%d. [%s] %s - %d vez/veces%n", pos++, m.getCodigo(), m.getTitulo(), reporte.cantidadPrestamos(m));
                }
                case 2 -> {
                    List<Usuario> deudores = reporte.usuariosConDeuda();
                    if (deudores.isEmpty()) System.out.println("Ningún usuario tiene deuda.");
                    for (Usuario u : deudores)
                        System.out.printf("[%s] %s - S/ %.2f%n", u.getCodigo(), u.getNombre(), biblioteca.deudaDe(u));
                }
                case 3 -> {
                    List<Prestamo> vencidos = reporte.prestamosVencidos();
                    if (vencidos.isEmpty()) System.out.println("No hay préstamos vencidos.");
                    for (Prestamo p : vencidos)
                        System.out.printf("%s tiene \"%s\" (vencía el %s, %d días de retraso)%n", p.getUsuario().getNombre(),
                                p.getMaterial().getTitulo(), p.getFechaVencimiento().format(FECHA), p.diasRetraso(biblioteca.hoy()));
                }
                case 4 -> accion(() -> {
                    String ruta = leerTexto("Archivo de salida [reporte_biblioteca.txt]: ");
                    if (ruta.isEmpty()) ruta = "reporte_biblioteca.txt";
                    if (!ruta.toLowerCase().endsWith(".txt")) ruta += ".txt";
                    reporte.exportarReporteTXT(ruta);
                    System.out.println("Reporte exportado en " + ruta);
                });
                case 0 -> volver = true;
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    // COPIAS DE SEGURIDAD
    private void menuRespaldo() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- COPIAS DE SEGURIDAD ---");
            System.out.println("1. Crear copia de seguridad   2. Restaurar copia de seguridad   0. Volver");
            switch (leerEntero("Opción: ")) {
                case 1 -> {
                    String ruta = leerTexto("Archivo de la copia [datos/copia_seguridad.dat]: ");
                    if (ruta.isEmpty()) ruta = "datos/copia_seguridad.dat";
                    try {
                        Respaldo.guardar(biblioteca, ruta);
                        System.out.println("Copia creada en " + ruta);
                    } catch (Exception e) {
                        System.out.println("No se pudo crear la copia: " + e.getMessage());
                    }
                }
                case 2 -> {
                    String ruta = leerTexto("Archivo de la copia: ");
                    if (!Respaldo.existe(ruta)) { System.out.println("No existe ese archivo."); break; }
                    if (!leerTexto("Se reemplazarán los datos actuales. ¿Continuar? (s/n): ").toLowerCase().startsWith("s")) break;
                    try {
                        biblioteca = Respaldo.cargar(ruta);
                        System.out.println("Copia restaurada.");
                    } catch (Exception e) {
                        System.out.println("No se pudo restaurar la copia: " + e.getMessage());
                    }
                }
                case 0 -> volver = true;
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    // AUXILIARES
    // Ejecuta una operación y muestra el aviso si una regla de negocio la rechaza. 
    private void accion(Runnable operacion) {
        try {
            operacion.run();
        } catch (BibliotecaException e) {
            System.out.println("Aviso: " + e.getMessage());
        }
    }

    private String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return in.nextLine().trim();
    }

    private int leerEntero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Escriba un número entero.");
            }
        }
    }

    private static String cortar(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
