package biblioteca.servicio;

import biblioteca.modelo.*;

import java.io.Serializable;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

//Clase principal. Agregación: la biblioteca CONTIENE materiales, usuarios, préstamos y reservas.
//Los materiales se guardan en un ArrayList y los usuarios en un HashMap (clave: código).

public class Biblioteca implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nombre;
    private ArrayList<Material> materiales = new ArrayList<>();
    private HashMap<String, Usuario> usuarios = new HashMap<>();
    private ArrayList<Prestamo> prestamos = new ArrayList<>();
    private ArrayList<Reserva> reservas = new ArrayList<>();
    private transient Supplier<LocalDate> reloj;   // permite simular fechas en datos de ejemplo y pruebas

    public Biblioteca(String nombre) { this.nombre = nombre; }

    // Materiales
    public void registrarMaterial(Material m) {
        if (m.getCodigo() == null || m.getCodigo().isBlank())
            throw new BibliotecaException("El código del material es obligatorio.");
        if (m.getTitulo() == null || m.getTitulo().isBlank())
            throw new BibliotecaException("El título del material es obligatorio.");
        if (buscarMaterialPorCodigo(m.getCodigo()) != null)
            throw new BibliotecaException("Ya existe un material con el código " + m.getCodigo() + ".");
        materiales.add(m);
    }

    //Busca por título, autor o código sin importar mayúsculas ni tildes.
    public ArrayList<Material> buscarMaterial(String criterio) {
        String q = normalizar(criterio);
        ArrayList<Material> encontrados = new ArrayList<>();
        for (Material m : materiales) {
            if (q.isEmpty() || normalizar(m.getTitulo()).contains(q)
                    || normalizar(m.getAutor()).contains(q) || normalizar(m.getCodigo()).contains(q))
                encontrados.add(m);
        }
        return encontrados;
    }

    public boolean editarMaterial(String codigo, String titulo, String autor) {
        Material m = buscarMaterialPorCodigo(codigo);
        if (m == null) return false;
        if (titulo != null && !titulo.isBlank()) m.setTitulo(titulo);
        if (autor != null && !autor.isBlank()) m.setAutor(autor);
        return true;
    }

    //Da de baja un material. Devuelve false si no existe o si está prestado.
    public boolean eliminarMaterial(String codigo) {
        Material m = buscarMaterialPorCodigo(codigo);
        if (m == null || !m.isDisponible()) return false;
        materiales.remove(m);
        for (Reserva r : reservas)
            if (r.getMaterial() == m && r.getEstado() == EstadoReserva.PENDIENTE) r.cancelar();
        return true;
    }

    public Material buscarMaterialPorCodigo(String codigo) {
        if (codigo == null) return null;
        for (Material m : materiales) if (m.getCodigo().equalsIgnoreCase(codigo.trim())) return m;
        return null;
    }

    // Usuarios
    public void registrarUsuario(Usuario u) {
        if (u.getCodigo() == null || u.getCodigo().isBlank())
            throw new BibliotecaException("El código del usuario es obligatorio.");
        if (u.getNombre() == null || u.getNombre().isBlank())
            throw new BibliotecaException("El nombre del usuario es obligatorio.");
        if (usuarios.containsKey(u.getCodigo()))
            throw new BibliotecaException("Ya existe un usuario con el código " + u.getCodigo() + ".");
        usuarios.put(u.getCodigo(), u);
    }

    public Usuario buscarUsuario(String codigo) {
        return codigo == null ? null : usuarios.get(codigo.trim());
    }

    // Préstamos
    public Prestamo registrarPrestamo(String codUsuario, String codMaterial) {
        Usuario u = exigirUsuario(codUsuario);
        Material m = exigirMaterial(codMaterial);
        double deuda = deudaDe(u);
        if (deuda > 0)
            throw new BibliotecaException("El usuario tiene multas pendientes (S/ " + String.format("%.2f", deuda) + ").");
        if (!u.puedePrestar(prestamosActivosDe(u).size()))
            throw new BibliotecaException("El usuario alcanzó su límite de " + u.obtenerLimitePrestamos() + " préstamos.");
        if (!m.isDisponible())
            throw new BibliotecaException("El material está prestado. Puede reservarlo.");
        Reserva primera = primeraReservaPendiente(m);
        if (primera != null && primera.getUsuario() != u)
            throw new BibliotecaException("El material está reservado para " + primera.getUsuario().getNombre() + ".");
        if (primera != null) primera.atender();

        LocalDate hoy = hoy();
        Prestamo p = new Prestamo(u, m, hoy, hoy.plusDays(u.obtenerPlazoPrestamo()));
        prestamos.add(p);
        m.setDisponible(false);
        return p;
    }

    // Registra la devolución del material. Devuelve la multa generada o null si llegó a tiempo
    public Multa registrarDevolucion(String codMaterial) {
        Material m = exigirMaterial(codMaterial);
        for (Prestamo p : prestamos) {
            if (p.getMaterial() == m && !p.estaDevuelto()) {
                p.registrarDevolucion(hoy());
                m.setDisponible(true);
                return p.getMulta();
            }
        }
        throw new BibliotecaException("Ese material no está prestado.");
    }

    public Reserva reservarMaterial(String codUsuario, String codMaterial) {
        Usuario u = exigirUsuario(codUsuario);
        Material m = exigirMaterial(codMaterial);
        if (m.isDisponible())
            throw new BibliotecaException("El material está disponible: puede prestarse directamente.");
        for (Prestamo p : prestamosActivosDe(u))
            if (p.getMaterial() == m) throw new BibliotecaException("El usuario ya tiene prestado ese material.");
        for (Reserva r : reservas)
            if (r.getMaterial() == m && r.getUsuario() == u && r.getEstado() == EstadoReserva.PENDIENTE)
                throw new BibliotecaException("El usuario ya reservó ese material.");
        Reserva r = new Reserva(u, m, hoy());
        reservas.add(r);
        return r;
    }

    public ArrayList<Prestamo> obtenerPrestamosVencidos() {
        ArrayList<Prestamo> vencidos = new ArrayList<>();
        for (Prestamo p : prestamos) if (p.estaVencido(hoy())) vencidos.add(p);
        return vencidos;
    }

    // Apoyo
    public ArrayList<Prestamo> prestamosActivosDe(Usuario u) {
        ArrayList<Prestamo> activos = new ArrayList<>();
        for (Prestamo p : prestamos) if (p.getUsuario() == u && !p.estaDevuelto()) activos.add(p);
        return activos;
    }

    //Suma de las multas no pagadas del usuario.
    public double deudaDe(Usuario u) {
        double total = 0;
        for (Prestamo p : prestamos) {
            Multa m = p.getMulta();
            if (p.getUsuario() == u && m != null && !m.isPagada()) total += m.getMonto();
        }
        return total;
    }

    //Marca como pagadas todas las multas del usuario y devuelve el monto pagado.
    public double pagarMultas(String codUsuario) {
        Usuario u = exigirUsuario(codUsuario);
        double total = deudaDe(u);
        if (total == 0) throw new BibliotecaException("El usuario no tiene multas pendientes.");
        for (Prestamo p : prestamos)
            if (p.getUsuario() == u && p.getMulta() != null) p.getMulta().pagar();
        return total;
    }

    public ArrayList<Reserva> reservasPendientes() {
        ArrayList<Reserva> pendientes = new ArrayList<>();
        for (Reserva r : reservas) if (r.getEstado() == EstadoReserva.PENDIENTE) pendientes.add(r);
        return pendientes;
    }

    public String getNombre() { return nombre; }
    public ArrayList<Material> getMateriales() { 
        return materiales; 
    }
    public ArrayList<Prestamo> getPrestamos() { 
        return prestamos; 
    }
    public ArrayList<Reserva> getReservas() { 
        return reservas; 
    }

    public List<Usuario> getUsuarios() {
        List<Usuario> lista = new ArrayList<>(usuarios.values());
        lista.sort(Comparator.comparing(Usuario::getCodigo));
        return lista;
    }

    public LocalDate hoy() { return reloj == null ? LocalDate.now() : reloj.get(); }
    public void setReloj(Supplier<LocalDate> reloj) { this.reloj = reloj; }

    // Privados
    private Reserva primeraReservaPendiente(Material m) {
        for (Reserva r : reservas)
            if (r.getMaterial() == m && r.getEstado() == EstadoReserva.PENDIENTE) return r;
        return null;
    }

    private Usuario exigirUsuario(String codigo) {
        Usuario u = buscarUsuario(codigo);
        if (u == null) throw new BibliotecaException("No existe el usuario con código " + codigo + ".");
        return u;
    }

    private Material exigirMaterial(String codigo) {
        Material m = buscarMaterialPorCodigo(codigo);
        if (m == null) throw new BibliotecaException("No existe el material con código " + codigo + ".");
        return m;
    }

    private static String normalizar(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase().trim();
    }
}
