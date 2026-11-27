package biblioteca.modelo;

import java.io.Serializable;
import java.time.LocalDate;

public class Reserva implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate fechaReserva;
    private EstadoReserva estado;
    private Material material;
    private Usuario usuario;

    public Reserva(Usuario usuario, Material material, LocalDate fechaReserva) {
        this.usuario = usuario;
        this.material = material;
        this.fechaReserva = fechaReserva;
        this.estado = EstadoReserva.PENDIENTE;
    }

    public void atender() { this.estado = EstadoReserva.ATENDIDA; }
    public void cancelar() { this.estado = EstadoReserva.CANCELADA; }

    public LocalDate getFechaReserva() { return fechaReserva; }
    public EstadoReserva getEstado() { return estado; }
    public Material getMaterial() { return material; }
    public Usuario getUsuario() { return usuario; }
}
