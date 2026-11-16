import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public class ServidorLoja {
    private static final String ARQUIVO_DADOS = "estoque_mercado.csv";
    private static final List<Produto> estoque = new ArrayList<>();
    private static int proximoId = 1;

    public static void main(String[] args) throws IOException {
        carregarDadosDoArquivo();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", exchange -> {
            Path htmlPath = Paths.get("index.html");
            if (!Files.exists(htmlPath)) {
                enviarResposta(exchange, 404, "Arquivo index.html não encontrado na pasta.", "text/plain");
                return;
            }
            byte[] htmlBytes = Files.readAllBytes(htmlPath);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, htmlBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(htmlBytes);
            }
        });

        server.createContext("/api/produtos", exchange -> {
            try {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    String json = gerarJsonEstoque();
                    enviarResposta(exchange, 200, json, "application/json");

                } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    String corpo = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    Map<String, String> dados = parseFormData(corpo);

                    String nome = dados.get("nome");
                    String categoria = dados.get("categoria");
                    int qtd = Integer.parseInt(dados.get("quantidade"));
                    double preco = Double.parseDouble(dados.get("preco").replace(",", "."));
                    LocalDate validade = LocalDate.parse(dados.get("dataValidade"));

                    synchronized (estoque) {
                        Produto novo = new Produto(proximoId++, nome, categoria, qtd, preco, validade);
                        estoque.add(novo);
                        salvarDadosNoArquivo();
                    }

                    enviarResposta(exchange, 201, "{\"mensagem\":\"Lote cadastrado com sucesso!\"}", "application/json");
                } else {
                    enviarResposta(exchange, 405, "{\"erro\":\"Método não permitido.\"}", "application/json");
                }
            } catch (DateTimeParseException e) {
                enviarResposta(exchange, 400, "{\"erro\":\"Data de validade inválida.\"}", "application/json");
            } catch (NumberFormatException e) {
                enviarResposta(exchange, 400, "{\"erro\":\"Verifique os números digitados.\"}", "application/json");
            } catch (IllegalArgumentException e) {
                enviarResposta(exchange, 400, "{\"erro\":\"" + e.getMessage().replace("\"", "'") + "\"}", "application/json");
            } catch (Exception e) {
                enviarResposta(exchange, 500, "{\"erro\":\"Erro interno no servidor.\"}", "application/json");
            }
        });

        server.createContext("/api/quantidade", exchange -> {
            try {
                Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                int id = Integer.parseInt(params.get("id"));
                int delta = Integer.parseInt(params.get("delta"));

                synchronized (estoque) {
                    Iterator<Produto> it = estoque.iterator();
                    while (it.hasNext()) {
                        Produto p = it.next();
                        if (p.getId() == id) {
                            int novaQtd = p.getQuantidade() + delta;
                            if (novaQtd <= 0) {
                                it.remove();
                            } else {
                                p.setQuantidade(novaQtd);
                            }
                            salvarDadosNoArquivo();
                            break;
                        }
                    }
                }
                enviarResposta(exchange, 200, "{\"mensagem\":\"Quantidade atualizada.\"}", "application/json");
            } catch (Exception e) {
                enviarResposta(exchange, 400, "{\"erro\":\"Erro ao atualizar quantidade.\"}", "application/json");
            }
        });

        server.createContext("/api/excluir", exchange -> {
            try {
                Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                if (params.containsKey("id")) {
                    int id = Integer.parseInt(params.get("id"));
                    synchronized (estoque) {
                        boolean removido = estoque.removeIf(p -> p.getId() == id);
                        if (removido) {
                            salvarDadosNoArquivo();
                        }
                    }
                }
                enviarResposta(exchange, 200, "{\"mensagem\":\"Lote removido da prateleira.\"}", "application/json");
            } catch (Exception e) {
                enviarResposta(exchange, 400, "{\"erro\":\"ID inválido para exclusão.\"}", "application/json");
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("====================================================");
        System.out.println(" Sistema Mercado Pro (Java) rodando!");
        System.out.println(" Acesse no navegador: http://localhost:8080");
        System.out.println("====================================================");
    }

    private static String gerarJsonEstoque() {
        synchronized (estoque) {
            List<Produto> ordenados = new ArrayList<>(estoque);
            ordenados.sort(Comparator.comparing(Produto::getDataValidade));

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < ordenados.size(); i++) {
                json.append(ordenados.get(i).toJson());
                if (i < ordenados.size() - 1) json.append(",");
            }
            json.append("]");
            return json.toString();
        }
    }

    private static void salvarDadosNoArquivo() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(ARQUIVO_DADOS), StandardCharsets.UTF_8)) {
            for (Produto p : estoque) {
                String nomeLimpo = p.getNome().replace(";", ",");
                String catLimpa = p.getCategoria().replace(";", ",");
                writer.write(String.format(Locale.US, "%d;%s;%s;%d;%.2f;%s",
                        p.getId(), nomeLimpo, catLimpa, p.getQuantidade(), p.getPreco(), p.getDataValidade().toString()));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar dados no arquivo: " + e.getMessage());
        }
    }

    private static void carregarDadosDoArquivo() {
        Path caminho = Paths.get(ARQUIVO_DADOS);
        if (!Files.exists(caminho)) return;

        try (BufferedReader reader = Files.newBufferedReader(caminho, StandardCharsets.UTF_8)) {
            String linha;
            int maiorId = 0;
            while ((linha = reader.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] partes = linha.split(";");
                if (partes.length == 6) {
                    int id = Integer.parseInt(partes[0]);
                    String nome = partes[1];
                    String categoria = partes[2];
                    int qtd = Integer.parseInt(partes[3]);
                    double preco = Double.parseDouble(partes[4]);
                    LocalDate validade = LocalDate.parse(partes[5]);

                    estoque.add(new Produto(id, nome, categoria, qtd, preco, validade));
                    if (id > maiorId) maiorId = id;
                }
            }
            proximoId = maiorId + 1;
        } catch (Exception e) {
            System.err.println("Erro ao carregar arquivo de dados: " + e.getMessage());
        }
    }

    private static void enviarResposta(HttpExchange exchange, int status, String resposta, String tipo) throws IOException {
        byte[] bytes = resposta.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", tipo + "; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        return parseFormData(query);
    }

    private static Map<String, String> parseFormData(String formData) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        for (String pair : formData.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                map.put(URLDecoder.decode(kv[0], "UTF-8"), URLDecoder.decode(kv[1], "UTF-8"));
            }
        }
        return map;
    }
}