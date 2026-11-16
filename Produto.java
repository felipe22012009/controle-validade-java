import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class Produto {
    private int id;
    private String nome;
    private String categoria;
    private int quantidade;
    private double preco;
    private LocalDate dataValidade;

    public Produto(int id, String nome, String categoria, int quantidade, double preco, LocalDate dataValidade) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do produto não pode ser vazio.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        if (preco < 0) {
            throw new IllegalArgumentException("O preço não pode ser negativo.");
        }
        if (dataValidade == null) {
            throw new IllegalArgumentException("A data de validade é obrigatória.");
        }

        this.id = id;
        this.nome = nome.trim();
        this.categoria = (categoria == null || categoria.trim().isEmpty()) ? "Geral" : categoria.trim();
        this.quantidade = quantidade;
        this.preco = preco;
        this.dataValidade = dataValidade;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getCategoria() { return categoria; }
    public int getQuantidade() { return quantidade; }
    public double getPreco() { return preco; }
    public LocalDate getDataValidade() { return dataValidade; }

    public void setQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        this.quantidade = quantidade;
    }

    public long getDiasParaVencer() {
        return ChronoUnit.DAYS.between(LocalDate.now(), dataValidade);
    }

    public String getDataFormatada() {
        return dataValidade.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getStatus() {
        long dias = getDiasParaVencer();
        if (dias <= 0) return "VENCIDO";
        if (dias <= 5) return "PROMOCAO";
        return "NORMAL";
    }

    public int getPorcentagemDesconto() {
        long dias = getDiasParaVencer();
        if (dias <= 0) return 0;
        if (dias <= 2) return 50;
        if (dias <= 5) return 30;
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
        if (getDiasParaVencer() <= 0) return 0.0;
        return getPrecoFinal() * quantidade;
    }

    public double getPrejuizoEstimado() {
        if (getDiasParaVencer() <= 0) return preco * quantidade;
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
            "{\"id\":%d,\"nome\":\"%s\",\"categoria\":\"%s\",\"quantidade\":%d,\"precoOriginal\":%.2f,\"precoFinal\":%.2f,\"desconto\":%d,\"subtotal\":%.2f,\"prejuizo\":%.2f,\"dataValidade\":\"%s\",\"dias\":%d,\"status\":\"%s\"}",
            id,
            escaparJson(nome),
            escaparJson(categoria),
            quantidade,
            preco,
            getPrecoFinal(),
            getPorcentagemDesconto(),
            getValorTotalLote(),
            getPrejuizoEstimado(),
            getDataFormatada(),
            getDiasParaVencer(),
            getStatus()
        );
    }
}