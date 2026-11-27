package biblioteca.modelo;

public class Docente extends Usuario {
    private static final long serialVersionUID = 1L;
    private String departamento;

    public Docente(String codigo, String nombre, String correo, String departamento) {
        super(codigo, nombre, correo);
        this.departamento = departamento;
    }

    @Override public int obtenerPlazoPrestamo() { return 15; }
    @Override public int obtenerLimitePrestamos() { return 5; }
    @Override public String obtenerTipo() { return "Docente"; }

    public String getDepartamento() { return departamento; }
}
