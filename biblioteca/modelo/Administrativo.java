package biblioteca.modelo;

public class Administrativo extends Usuario {
    private static final long serialVersionUID = 1L;
    private String area;

    public Administrativo(String codigo, String nombre, String correo, String area) {
        super(codigo, nombre, correo);
        this.area = area;
    }

    @Override public int obtenerPlazoPrestamo() { return 10; }
    @Override public int obtenerLimitePrestamos() { return 4; }
    @Override public String obtenerTipo() { return "Administrativo"; }

    public String getArea() { return area; }
}
