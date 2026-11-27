package biblioteca.modelo;

public class Tesis extends Material {
    private static final long serialVersionUID = 1L;
    private String asesor;
    private String grado;

    public Tesis(String codigo, String titulo, String autor, int anio, String asesor, String grado) {
        super(codigo, titulo, autor, anio);
        this.asesor = asesor;
        this.grado = grado;
    }

    @Override public String obtenerTipo() { return "Tesis"; }

    public String getAsesor() { return asesor; }
    public String getGrado() { return grado; }
}
