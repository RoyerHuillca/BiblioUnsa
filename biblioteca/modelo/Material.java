package biblioteca.modelo;

import java.io.Serializable;

/** Clase abstracta: cualquier material que la biblioteca puede prestar. */
public abstract class Material implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codigo;
    private String titulo;
    private String autor;
    private int anio;
    private boolean disponible;

    protected Material(String codigo, String titulo, String autor, int anio) {
        this.codigo = codigo;
        this.titulo = titulo;
        this.autor = autor;
        this.anio = anio;
        this.disponible = true;
    }

    /** Tipo del material: "Libro", "Tesis" o "Revista". */
    public abstract String obtenerTipo();

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean d) { this.disponible = d; }

    public String getCodigo() { return codigo; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
    public int getAnio() { return anio; }

    @Override
    public String toString() { return "[" + codigo + "] " + titulo; }
}
