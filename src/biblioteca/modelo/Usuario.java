package biblioteca.modelo;

import java.io.Serializable;

// Clase abstracta: cada tipo de usuario define su plazo y su límite de préstamos (polimorfismo).
public abstract class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codigo;
    private String nombre;
    private String correo;

    protected Usuario(String codigo, String nombre, String correo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.correo = correo;
    }

    // Días que dura un préstamo para este tipo de usuario.
    public abstract int obtenerPlazoPrestamo();

    // Cantidad máxima de préstamos activos al mismo tiempo.
    public abstract int obtenerLimitePrestamos();

    // Tipo del usuario: "Estudiante", "Docente" o "Administrativo".
    public abstract String obtenerTipo();

    public boolean puedePrestar(int prestamosActivos) {
        return prestamosActivos < obtenerLimitePrestamos();
    }

    public String getCodigo() { 
        return codigo; 
    }
    public String getNombre() { 
        return nombre; 
    }
    public void setNombre(String nombre) { 
        this.nombre = nombre; 
    }
    public String getCorreo() { 
        return correo; 
    }
    public void setCorreo(String correo) { 
        this.correo = correo; 
    }

    @Override
    public String toString() { 
        return "[" + codigo + "] " + nombre; 
    }
}
