FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .
RUN javac -encoding UTF-8 Produto.java ServidorLoja.java
EXPOSE 8080
CMD ["java", "ServidorLoja"]