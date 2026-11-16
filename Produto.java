public class Produto {
    private String nome;
    private int quantidade;
    private double preco;
    private int diasParaVencer;

    public Produto(String nome, int quantidade, double preco, int diasParaVencer) {
        this.nome = nome;
        this.quantidade = quantidade;
        this.preco = preco;
        this.diasParaVencer = diasParaVencer;
    }

    public String getNome() {
        return nome;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double getPreco() {
        return preco;
    }

    public int getDiasParaVencer() {
        return diasParaVencer;
    }

    public String verificarStatus() {
        if (diasParaVencer <= 0) {
            return "VENCIDO (Retirar da prateleira!)";
        } else if (diasParaVencer <= 5) {
            double precoPromocional = preco * 0.50; // 50% de desconto
            return String.format("PROMOÇÃO RELÂMPAGO (Vence em %d dias) | De R$ %.2f por R$ %.2f",
                    diasParaVencer, preco, precoPromocional);
        } else {
            return String.format("NORMAL (Vence em %d dias) | Preço: R$ %.2f",
                    diasParaVencer, preco);
        }
    }
}