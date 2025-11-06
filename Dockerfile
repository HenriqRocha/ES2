# --- Estágio 1: Construir o projeto ---
# Usamos uma imagem estável com Maven e Java 21 (LTS)
FROM maven:3.9.7-eclipse-temurin-21 AS build

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia o "mapa" do projeto e baixa as dependências
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copia o resto do código-fonte e constrói o .jar
COPY src ./src
RUN mvn -B package -DskipTests

# --- Estágio 2: Executar o projeto ---
# Usamos uma imagem leve, apenas com Java 21 (LTS)
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copia o .jar que foi construído no estágio anterior
COPY --from=build /app/target/*.jar app.jar

# O comando que o Render vai executar para ligar o servidor
ENTRYPOINT ["java", "-jar", "app.jar"]