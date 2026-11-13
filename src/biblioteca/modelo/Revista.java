package biblioteca.modelo;

public class Revista extends Material {
    private static final long serialVersionUID = 1L;
    private String issn;
    private int volumen;
    private int numero;

    public Revista(String codigo, String titulo, String autor, int anio, String issn, int volumen, int numero) {
        super(codigo, titulo, autor, anio);
        this.issn = issn;
        this.volumen = volumen;
        this.numero = numero;
    }

    @Override public String obtenerTipo() { 
        return "Revista"; 
    }

    public String getIssn() { 
        return issn; 
    }
    public int getVolumen() { 
        return volumen; 
    }
    public int getNumero() { 
        return numero; 
    }
}
