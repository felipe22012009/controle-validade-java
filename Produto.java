import java.util.Locale;

public class Produto {
    private int id;
    private String nome;
    private int quantidade;
    private double preco;
    private int diasParaVencer;

    public Produto(int id, String nome, int quantidade, double preco, int diasParaVencer) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do produto não pode ser vazio.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        if (preco < 0) {
            throw new IllegalArgumentException("O preço não pode ser negativo.");
        }

        this.id = id;
        this.nome = nome.trim();
        this.quantidade = quantidade;
        this.preco = preco;
        this.diasParaVencer = diasParaVencer;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public int getQuantidade() { return quantidade; }
    public double getPreco() { return preco; }
    public int getDiasParaVencer() { return diasParaVencer; }

    public String getStatus() {
        if (diasParaVencer <= 0) return "VENCIDO";
        if (diasParaVencer <= 5) return "PROMOCAO";
        return "NORMAL";
    }

    public int getPorcentagemDesconto() {
        if (diasParaVencer <= 0) return 0;
        if (diasParaVencer <= 2) return 50;
        if (diasParaVencer <= 5) return 30;
        return 0;
    }

    public double getPrecoFinal() {
        int desconto = getPorcentagemDesconto();
        if (desconto > 0) {
            return preco * (1.0 - (desconto / 100.0));
        }
        return preco;
    }

    public double getValorTotalLote() {
        if (diasParaVencer <= 0) return 0.0;
        return getPrecoFinal() * quantidade;
    }

    public double getPrejuizoEstimado() {
        if (diasParaVencer <= 0) return preco * quantidade;
        return 0.0;
    }

    private String escaparJson(String texto) {
        return texto.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", " ")
                    .replace("\r", " ");
    }

    public String toJson() {
        return String.format(Locale.US,
            "{\"id\":%d,\"nome\":\"%s\",\"quantidade\":%d,\"precoOriginal\":%.2f,\"precoFinal\":%.2f,\"desconto\":%d,\"subtotal\":%.2f,\"prejuizo\":%.2f,\"dias\":%d,\"status\":\"%s\"}",
            id,
            escaparJson(nome),
            quantidade,
            preco,
            getPrecoFinal(),
            getPorcentagemDesconto(),
            getValorTotalLote(),
            getPrejuizoEstimado(),
            diasParaVencer,
            getStatus()
        );
    }
}