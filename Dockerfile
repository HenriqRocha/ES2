# --- Estágio 1: Construir o projeto ---
# Usamos uma imagem que já tem Maven e Java 20 (LINHA CORRIGIDA)
FROM maven:3.9.7-eclipse-temurin-20 AS build

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia o "mapa" do projeto e baixa as dependências
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copia o resto do código-fonte e constrói o .jar
COPY src ./src
RUN mvn -B package -DskipTests

# --- Estágio 2: Executar o projeto ---
# Usamos uma imagem leve, apenas com Java 20
FROM eclipse-temurin:20-jre

WORKDIR /app

# Copia o .jar que foi construído no estágio anterior
COPY --from=build /app/target/*.jar app.jar

# O comando que o Render vai executar para ligar o servidor
ENTRYPOINT ["java", "-jar", "app.jar"]