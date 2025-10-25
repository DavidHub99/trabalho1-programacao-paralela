import java.io.Serializable;

public class Pedido extends Comunicado {
    private static final long serialVersionUID = 1L;

    private byte[] numeros;
    private int procurado;

    public Pedido(byte[] numeros, int procurado) {
        this.numeros = numeros;
        this.procurado = procurado;
    }

    public byte[] getNumeros() {
        return numeros;
    }

    public int getProcurado() {
        return procurado;
    }

}

