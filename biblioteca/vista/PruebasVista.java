package biblioteca.vista;

import biblioteca.modelo.*;
import biblioteca.servicio.*;

import java.awt.Component;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Pruebas de la interfaz gráfica sin abrir ventanas: se crean los paneles, se llenan los campos,
 * se pulsan los botones con doClick() y se revisan las tablas y los mensajes.
 * Ejecutar con ./probar.sh
 */
public class PruebasVista {
    static int ok = 0, fallos = 0;

    static void verificar(String nombre, boolean condicion) {
        if (condicion) { ok++; System.out.println("  OK    " + nombre); }
        else { fallos++; System.out.println("  FALLO " + nombre); }
    }

    /** Salida falsa: guarda los mensajes que la interfaz habría mostrado. */
    static class SalidaFalsa implements Mensajes.Salida {
        final List<String> info = new ArrayList<>(), avisos = new ArrayList<>();
        final Deque<String> respuestas = new ArrayDeque<>();
        boolean confirmacion = true;
        @Override public void info(Component p, String m) { info.add(m); }
        @Override public void aviso(Component p, String m) { avisos.add(m); }
        @Override public boolean confirmar(Component p, String m) { return confirmacion; }
        @Override public String pedir(Component p, String m, String inicial) { return respuestas.poll(); }
        String ultimoInfo() { return info.isEmpty() ? "" : info.get(info.size() - 1); }
        String ultimoAviso() { return avisos.isEmpty() ? "" : avisos.get(avisos.size() - 1); }
    }

    static boolean seleccionarFila(javax.swing.JTable tabla, int columna, String valor) {
        for (int i = 0; i < tabla.getRowCount(); i++) {
            if (valor.equals(String.valueOf(tabla.getValueAt(i, columna)))) {
                tabla.setRowSelectionInterval(i, i);
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        SalidaFalsa salida = new SalidaFalsa();
        Mensajes.usar(salida);

        Sesion sesion = new Sesion(DatosEjemplo.crear());
        PanelMateriales pm = new PanelMateriales(sesion);
        PanelUsuarios pu = new PanelUsuarios(sesion);
        PanelPrestamos pp = new PanelPrestamos(sesion);
        PanelReportes pr = new PanelReportes(sesion);
        sesion.refrescar = () -> { pm.actualizar(); pu.actualizar(); pp.actualizar(); pr.actualizar(); };
        Biblioteca b = sesion.biblioteca;

        System.out.println("Pestaña Materiales");
        verificar("muestra los 9 materiales", pm.modelo.getRowCount() == 9);
        pm.txtBusqueda.setText("garcia");
        verificar("la búsqueda 'garcia' deja 1 fila", pm.modelo.getRowCount() == 1);
        pm.txtBusqueda.setText("");
        verificar("al borrar la búsqueda vuelven las 9 filas", pm.modelo.getRowCount() == 9);
        pm.registrar(new Libro("L050", "Estructuras de Datos", "N. Wirth", 1986, "111", "Prentice"));
        verificar("registrar agrega una fila", pm.modelo.getRowCount() == 10 && b.buscarMaterialPorCodigo("L050") != null);
        pm.registrar(new Libro("L050", "Repetido", "X", 2000, "1", "Z"));
        verificar("código repetido muestra aviso", salida.ultimoAviso().contains("Ya existe"));
        seleccionarFila(pm.tabla, 0, "L050");
        salida.respuestas.add("Estructuras de Datos II");
        salida.respuestas.add("Niklaus Wirth");
        pm.btnEditar.doClick();
        verificar("editar cambia título y autor", b.buscarMaterialPorCodigo("L050").getTitulo().equals("Estructuras de Datos II")
                && b.buscarMaterialPorCodigo("L050").getAutor().equals("Niklaus Wirth"));
        pm.tabla.clearSelection();
        pm.btnEditar.doClick();
        verificar("editar sin selección avisa", salida.ultimoAviso().contains("Seleccione"));
        seleccionarFila(pm.tabla, 0, "L003");
        pm.btnBaja.doClick();
        verificar("baja de material prestado avisa y no lo elimina", salida.ultimoAviso().contains("prestado")
                && b.buscarMaterialPorCodigo("L003") != null);
        seleccionarFila(pm.tabla, 0, "L050");
        pm.btnBaja.doClick();
        verificar("baja de material disponible lo elimina", b.buscarMaterialPorCodigo("L050") == null && pm.modelo.getRowCount() == 9);
        seleccionarFila(pm.tabla, 0, "L004");
        salida.confirmacion = false;
        pm.btnBaja.doClick();
        salida.confirmacion = true;
        verificar("si no se confirma, no se da de baja", b.buscarMaterialPorCodigo("L004") != null);

        System.out.println("Formularios");
        FormularioMaterial fm = new FormularioMaterial();
        fm.txtCodigo.setText("T050"); fm.txtTitulo.setText("Tesis X"); fm.txtAutor.setText("Ana"); fm.txtAnio.setText("2025");
        fm.cmbTipo.setSelectedItem("Tesis"); fm.txtExtra1.setText("Dr. Paredes"); fm.txtExtra2.setText("Ingeniero");
        Material t = fm.construir();
        verificar("formulario arma una Tesis", t instanceof Tesis && ((Tesis) t).getAsesor().equals("Dr. Paredes"));
        fm.cmbTipo.setSelectedItem("Revista"); fm.txtExtra1.setText("1111-2222"); fm.txtExtra2.setText("3"); fm.txtExtra3.setText("2");
        Material r = fm.construir();
        verificar("formulario arma una Revista", r instanceof Revista && ((Revista) r).getVolumen() == 3 && ((Revista) r).getNumero() == 2);
        fm.txtAnio.setText("dos mil");
        boolean error = false;
        try { fm.construir(); } catch (IllegalArgumentException e) { error = e.getMessage().contains("año") || e.getMessage().contains("Año"); }
        verificar("año no numérico es rechazado", error);
        fm.txtAnio.setText("2025"); fm.txtCodigo.setText("");
        error = false;
        try { fm.construir(); } catch (IllegalArgumentException e) { error = e.getMessage().contains("código"); }
        verificar("código vacío es rechazado", error);
        FormularioUsuario fu = new FormularioUsuario();
        fu.cmbTipo.setSelectedItem("Docente");
        fu.txtCodigo.setText("D050"); fu.txtNombre.setText("Luis Paredes"); fu.txtCorreo.setText("l@unsa.edu.pe"); fu.txtExtra.setText("Sistemas");
        Usuario nuevo = fu.construir();
        verificar("formulario arma un Docente", nuevo instanceof Docente && ((Docente) nuevo).getDepartamento().equals("Sistemas"));

        System.out.println("Pestaña Usuarios");
        verificar("muestra los 4 usuarios", pu.modelo.getRowCount() == 4);
        pu.txtBusqueda.setText("E002");
        pu.btnBuscar.doClick();
        verificar("buscar por código deja 1 fila con deuda S/ 13.00", pu.modelo.getRowCount() == 1
                && String.valueOf(pu.modelo.getValueAt(0, 7)).equals("S/ 13.00"));
        pu.btnTodos.doClick();
        verificar("'Ver todos' vuelve a mostrar los 4", pu.modelo.getRowCount() == 4);
        pu.registrar(nuevo);
        verificar("registrar usuario agrega una fila", pu.modelo.getRowCount() == 5);
        pu.registrar(nuevo);
        verificar("usuario repetido muestra aviso", salida.ultimoAviso().contains("Ya existe"));
        seleccionarFila(pu.tabla, 0, "E002");
        pu.btnPagar.doClick();
        verificar("pagar multas deja la deuda en 0", salida.ultimoInfo().contains("13.00")
                && b.deudaDe(b.buscarUsuario("E002")) == 0);
        seleccionarFila(pu.tabla, 0, "E002");   // la tabla pierde la selección al refrescar
        pu.btnPagar.doClick();
        verificar("pagar sin deuda muestra aviso", salida.ultimoAviso().contains("no tiene multas"));

        System.out.println("Pestaña Préstamos y reservas");
        verificar("combos con 5 usuarios y 9 materiales", pp.cmbUsuario.getItemCount() == 5 && pp.cmbMaterial.getItemCount() == 9);
        verificar("tabla con los 4 préstamos de ejemplo", pp.modeloPrestamos.getRowCount() == 4);
        pp.cmbUsuario.setSelectedItem(b.buscarUsuario("E001"));
        pp.cmbMaterial.setSelectedItem(b.buscarMaterialPorCodigo("L004"));
        pp.btnPrestar.doClick();
        verificar("registrar préstamo agrega fila e informa", pp.modeloPrestamos.getRowCount() == 5
                && salida.ultimoInfo().contains("Préstamo registrado") && !b.buscarMaterialPorCodigo("L004").isDisponible());
        pp.cmbUsuario.setSelectedItem(b.buscarUsuario("D001"));
        pp.cmbMaterial.setSelectedItem(b.buscarMaterialPorCodigo("L004"));
        pp.btnPrestar.doClick();
        verificar("prestar un material ya prestado avisa", salida.ultimoAviso().contains("prestado"));
        pp.cmbUsuario.setSelectedItem(b.buscarUsuario("A001"));
        pp.btnReservar.doClick();
        verificar("reservar agrega una reserva pendiente", pp.modeloReservas.getRowCount() == 1);
        pp.btnReservar.doClick();
        verificar("reservar dos veces avisa", salida.ultimoAviso().contains("ya reservó"));
        seleccionarFila(pp.tablaReservas, 1, "Los ríos profundos");
        pp.btnCancelar.doClick();
        verificar("cancelar reserva la quita de la tabla", pp.modeloReservas.getRowCount() == 0);
        pp.tablaPrestamos.clearSelection();
        pp.btnDevolver.doClick();
        verificar("devolver sin selección avisa", salida.ultimoAviso().contains("Seleccione"));
        seleccionarFila(pp.tablaPrestamos, 2, "Introduction to Algorithms");
        pp.btnDevolver.doClick();
        verificar("devolución tardía informa 10 días y S/ 10.00", salida.ultimoInfo().contains("10 día")
                && salida.ultimoInfo().contains("S/ 10.00"));
        verificar("el material vuelve a estar disponible", b.buscarMaterialPorCodigo("L003").isDisponible());
        pp.chkSoloActivos.doClick();
        verificar("'Solo activos' oculta los devueltos", pp.modeloPrestamos.getRowCount() == 3);
        pp.chkSoloActivos.doClick();

        System.out.println("Pestaña Reportes");
        verificar("libros más prestados con filas", pr.modeloTop.getRowCount() >= 4);
        verificar("usuarios con deuda: A001", pr.modeloDeuda.getRowCount() == 1 && "A001".equals(pr.modeloDeuda.getValueAt(0, 0)));
        verificar("sin préstamos vencidos tras la devolución", pr.modeloVencidos.getRowCount() == 0);
        File txt = new File("datos/reporte_vista_prueba.txt");
        pr.exportarA(txt);
        verificar("exporta el reporte a .txt", txt.exists() && txt.length() > 0 && salida.ultimoInfo().contains("exportado"));
        txt.delete();

        System.out.println("Base de datos (menú Archivo)");
        Biblioteca antes = sesion.biblioteca;
        java.io.File bd = new java.io.File(AccionesBD.ARCHIVO_BD);
        bd.delete();
        int avisosAntes = salida.avisos.size();
        AccionesBD.guardar(sesion, null);
        if (biblioteca.persistencia.BaseDatos.hayDriver()) {
            verificar("con el driver: guardar informa y crea el archivo .db", salida.ultimoInfo().contains("guardados") && bd.exists());
            AccionesBD.cargar(sesion, null);
            verificar("con el driver: cargar reemplaza la biblioteca con los mismos datos", sesion.biblioteca != antes
                    && sesion.biblioteca.getMateriales().size() == antes.getMateriales().size()
                    && sesion.biblioteca.getUsuarios().size() == antes.getUsuarios().size());
            bd.delete();
        } else {
            verificar("sin el driver: guardar muestra un aviso que menciona el driver", salida.avisos.size() == avisosAntes + 1
                    && salida.ultimoAviso().contains("driver"));
            AccionesBD.cargar(sesion, null);
            verificar("sin el driver: cargar avisa y conserva los datos actuales", salida.avisos.size() == avisosAntes + 2
                    && sesion.biblioteca == antes);
            verificar("sin el driver: no se crea ningún archivo .db", !bd.exists());
        }
        BarraMenu barra = new BarraMenu(sesion, null, () -> { });
        verificar("el menú Archivo tiene las opciones de base de datos", barra.itemGuardarBD.getText().contains("SQLite")
                && barra.itemCargarBD.getText().contains("SQLite") && barra.getMenuCount() == 2);
        int infosAntes = salida.info.size(), avisosMenu = salida.avisos.size();
        barra.itemGuardarBD.doClick();
        boolean reaccion = biblioteca.persistencia.BaseDatos.hayDriver() ? salida.info.size() == infosAntes + 1
                : salida.avisos.size() == avisosMenu + 1;
        verificar("pulsar 'Guardar en base de datos' responde con un mensaje", reaccion);
        bd.delete();
        barra.itemGuardar.doClick();
        verificar("pulsar 'Guardar ahora' guarda en datos/biblioteca.dat", salida.ultimoInfo().contains("biblioteca.dat")
                && new java.io.File(BarraMenu.ARCHIVO_DATOS).exists());
        new java.io.File(BarraMenu.ARCHIVO_DATOS).delete();
        salida.confirmacion = false;
        int avisosPrevios = salida.avisos.size();
        AccionesBD.cargar(sesion, null);
        salida.confirmacion = true;
        verificar("si no se confirma, cargar no hace nada", salida.avisos.size() == avisosPrevios && sesion.biblioteca == antes);

        System.out.println("\nResultado: " + ok + " correctas, " + fallos + " fallos");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
