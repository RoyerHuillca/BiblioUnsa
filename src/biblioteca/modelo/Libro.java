package biblioteca.modelo;

public class Libro extends Material {
    private static final long serialVersionUID = 1L;
    private String isbn;
    private String editorial;

    public Libro(String codigo, String titulo, String autor, int año, String isbn, String editorial) {
        super(codigo, titulo, autor, año);
        this.isbn = isbn;
        this.editorial = editorial;
    }

    @Override public String obtenerTipo() { 
        return "Libro"; 
    }

    public String getIsbn() { 
        return isbn; 
    }
    public String getEditorial() { 
        return editorial; 
    }
}
