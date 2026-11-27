package biblioteca.modelo;

import java.io.Serializable;

/** Multa por devolver tarde un material. Pertenece al préstamo que la genera (composición). */
public class Multa implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final double TARIFA_POR_DIA = 1.00;

    private int diasRetraso;
    private double monto;
    private boolean pagada;

    public Multa(int diasRetraso) {
        this.diasRetraso = diasRetraso;
        this.monto = calcularMonto();
        this.pagada = false;
    }

    public double calcularMonto() { return diasRetraso * TARIFA_POR_DIA; }

    public void pagar() { this.pagada = true; }

    public int getDiasRetraso() { return diasRetraso; }
    public double getMonto() { return monto; }
    public boolean isPagada() { return pagada; }
}
