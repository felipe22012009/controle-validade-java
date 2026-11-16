import java.util.ArrayList;
import java.util.Scanner;

public class ControleValidade {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArrayList<Produto> estoque = new ArrayList<>();
        int opcao = 0;

        while (opcao != 4) {
            System.out.println("\n=========================================");
            System.out.println("       CONTROLE DE VALIDADE - LOJA       ");
            System.out.println("=========================================");
            System.out.println("1. Cadastrar novo produto");
            System.out.println("2. Gerar relatório geral");
            System.out.println("3. Ver apenas alertas (Vencidos e Promoções)");
            System.out.println("4. Sair");
            System.out.print("Escolha uma opção: ");

            if (scanner.hasNextInt()) {
                opcao = scanner.nextInt();
                scanner.nextLine(); // Limpa o buffer
            } else {
                System.out.println("Por favor, digite apenas números.");
                scanner.nextLine();
                continue;
            }

            switch (opcao) {
                case 1:
                    System.out.print("\nNome do produto: ");
                    String nome = scanner.nextLine();

                    System.out.print("Quantidade em estoque: ");
                    int qtd = scanner.nextInt();

                    System.out.print("Preço unitário (ex: 12,50 ou 12.50): R$ ");
                    String precoTexto = scanner.next().replace(",", ".");
                    double preco = Double.parseDouble(precoTexto);

                    System.out.print("Dias faltando para vencer (use 0 ou negativo se já venceu): ");
                    int dias = scanner.nextInt();

                    estoque.add(new Produto(nome, qtd, preco, dias));
                    System.out.println("\nProduto cadastrado com sucesso!");
                    break;

                case 2:
                    System.out.println("\n=== RELATÓRIO GERAL DE ESTOQUE ===");
                    if (estoque.isEmpty()) {
                        System.out.println("Nenhum produto cadastrado no sistema.");
                    } else {
                        for (Produto p : estoque) {
                            System.out.println("- " + p.getNome() + " (" + p.getQuantidade() + " un.) -> " + p.verificarStatus());
                        }
                    }
                    break;

                case 3:
                    System.out.println("\n=== ALERTAS DE VALIDADE (AÇÃO URGENTE) ===");
                    boolean encontrouAlerta = false;
                    for (Produto p : estoque) {
                        if (p.getDiasParaVencer() <= 5) {
                            System.out.println("! " + p.getNome() + " (" + p.getQuantidade() + " un.) -> " + p.verificarStatus());
                            encontrouAlerta = true;
                        }
                    }
                    if (!encontrouAlerta) {
                        System.out.println("Nenhum produto vencido ou próximo do vencimento.");
                    }
                    break;

                case 4:
                    System.out.println("\nEncerrando o sistema. Até logo!");
                    break;

                default:
                    System.out.println("\nOpção inválida! Tente novamente.");
            }
        }

        scanner.close();
    }
} 