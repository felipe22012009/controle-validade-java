public class Produto {
    private int id;
    private String nome;
    private int quantidade;
    private double preco;
    private int diasParaVencer;

    public Produto(int id, String nome, int quantidade, double preco, int diasParaVencer) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.preco = preco;
        this.diasParaVencer = diasParaVencer;
    }

    public int getId() { return id; }

    public String getStatus() {
        if (diasParaVencer <= 0) return "VENCIDO";
        if (diasParaVencer <= 5) return "PROMOCAO";
        return "NORMAL";
    }

    public double getPrecoFinal() {
        if (diasParaVencer > 0 && diasParaVencer <= 5) {
            return preco * 0.50;
        }
        return preco;
    }

    public String toJson() {
        return String.format(java.util.Locale.US,
            "{\"id\":%d,\"nome\":\"%s\",\"quantidade\":%d,\"precoOriginal\":%.2f,\"precoFinal\":%.2f,\"dias\":%d,\"status\":\"%s\"}",
            id, nome.replace("\"", "\\\""), quantidade, preco, getPrecoFinal(), diasParaVencer, getStatus()
        );
    }
}