import java.io.Serializable;

public class Resposta extends Comunicado {
    private static final long serialVersionUID = 1L;

    private Integer contagem;

    public Resposta(int contagem) {
        this.contagem = contagem;
    }

    public Integer getContagem() {
        return contagem;
    }
}

