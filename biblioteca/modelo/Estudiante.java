package biblioteca.modelo;

public class Estudiante extends Usuario {
    private static final long serialVersionUID = 1L;
    private String escuela;

    public Estudiante(String codigo, String nombre, String correo, String escuela) {
        super(codigo, nombre, correo);
        this.escuela = escuela;
    }

    @Override public int obtenerPlazoPrestamo() { return 7; }
    @Override public int obtenerLimitePrestamos() { return 3; }
    @Override public String obtenerTipo() { return "Estudiante"; }

    public String getEscuela() { return escuela; }
}
