package biblioteca.servicio;

/** Error de regla de negocio (usuario con multa, material no disponible, datos repetidos, etc.). */
public class BibliotecaException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public BibliotecaException(String mensaje) { super(mensaje); }
}
