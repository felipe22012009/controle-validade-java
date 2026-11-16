public class ServidorLoja {
    private static List<Produto> estoque = new ArrayList<>();
    private static int proximoId = 1;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", exchange -> {
            byte[] htmlBytes = Files.readAllBytes(Paths.get("index.html"));
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, htmlBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(htmlBytes);
            }
        });

        server.createContext("/api/produtos", exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < estoque.size(); i++) {
                    json.append(estoque.get(i).toJson());
                    if (i < estoque.size() - 1) json.append(",");
                }
                json.append("]");
                enviarResposta(exchange, 200, json.toString(), "application/json");

            } else if ("POST".equals(exchange.getRequestMethod())) {
                String corpo = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> dados = parseFormData(corpo);

                String nome = dados.get("nome");
                int qtd = Integer.parseInt(dados.get("quantidade"));
                double preco = Double.parseDouble(dados.get("preco"));
                int dias = Integer.parseInt(dados.get("dias"));

                estoque.add(new Produto(proximoId++, nome, qtd, preco, dias));
                enviarResposta(exchange, 201, "{\"mensagem\":\"Cadastrado\"}", "application/json");
            }
        });

        server.createContext("/api/excluir", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.startsWith("id=")) {
                int id = Integer.parseInt(query.split("=")[1]);
                estoque.removeIf(p -> p.getId() == id);
            }
            enviarResposta(exchange, 200, "{\"mensagem\":\"Removido\"}", "application/json");
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Servidor Java rodando! Acesse: http://localhost:8080");
    }

    private static void enviarResposta(HttpExchange exchange, int status, String resposta, String tipo) throws IOException {
        byte[] bytes = resposta.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", tipo + "; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
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