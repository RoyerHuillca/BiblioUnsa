package biblioteca.modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Prestamo implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate fechaPrestamo;
    private LocalDate fechaVencimiento;
    private LocalDate fechaDevolucion;
    private EstadoPrestamo estado;
    private Material material;
    private Usuario usuario;
    private Multa multa;          // composición: 0..1

    public Prestamo(Usuario usuario, Material material, LocalDate fechaPrestamo, LocalDate fechaVencimiento) {
        this.usuario = usuario;
        this.material = material;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = EstadoPrestamo.ACTIVO;
    }

    /** Registra la devolución; si hubo retraso genera la multa. */
    public void registrarDevolucion(LocalDate fecha) {
        this.fechaDevolucion = fecha;
        this.estado = EstadoPrestamo.DEVUELTO;
        if (diasRetraso(fecha) > 0) generarMulta(fecha);
    }

    /** Indica si el préstamo no devuelto ya pasó su fecha de vencimiento (y lo marca como VENCIDO). */
    public boolean estaVencido(LocalDate hoy) {
        if (estado == EstadoPrestamo.DEVUELTO) return false;
        if (hoy.isAfter(fechaVencimiento)) {
            estado = EstadoPrestamo.VENCIDO;
            return true;
        }
        return false;
    }

    public int diasRetraso(LocalDate fecha) {
        long dias = ChronoUnit.DAYS.between(fechaVencimiento, fecha);
        return (int) Math.max(0, dias);
    }

    public Multa generarMulta(LocalDate fecha) {
        this.multa = new Multa(diasRetraso(fecha));
        return multa;
    }

    public boolean estaDevuelto() { return estado == EstadoPrestamo.DEVUELTO; }

    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public EstadoPrestamo getEstado() { return estado; }
    public Material getMaterial() { return material; }
    public Usuario getUsuario() { return usuario; }
    public Multa getMulta() { return multa; }
}
