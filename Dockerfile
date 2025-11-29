# ========================
# STAGE 1: BUILD APP
# ========================
# Dùng Maven kèm Java 17 bản Temurin (Mới và ổn định hơn)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy code vào
COPY . .

# Build
RUN mvn clean package -DskipTests

# ========================
# STAGE 2: RUN APP
# ========================
# Dùng Eclipse Temurin JRE 17 (Siêu nhẹ để chạy web)
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy file jar đã build
COPY --from=build /app/target/*.jar app.jar

# Copy folder ảnh demo (Quan trọng)
COPY uploads /app/uploads

# Mở cổng
EXPOSE 8080

# Chạy
ENTRYPOINT ["java", "-jar", "app.jar"]
