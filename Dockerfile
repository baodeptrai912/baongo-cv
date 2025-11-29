# ========================
# STAGE 1: BUILD APP (Thợ xây)
# ========================
# Dùng Maven và Java 17 để biên dịch code
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copy toàn bộ code vào trong máy ảo Docker
COPY . .

# Chạy lệnh build (bỏ qua test cho nhanh và đỡ lỗi vặt)
RUN mvn clean package -DskipTests

# ========================
# STAGE 2: RUN APP (Người vận hành)
# ========================
# Dùng bản Java 17 siêu nhẹ để chạy
FROM openjdk:17-jdk-slim
WORKDIR /app

# 1. Copy file .jar đã build từ Stage 1 sang đây
# (Lệnh này tự tìm file .jar bất kể tên là gì và đổi tên thành app.jar cho gọn)
COPY --from=build /app/target/*.jar app.jar

# 2. QUAN TRỌNG: Copy folder uploads (chứa ảnh demo) vào server
# Để đảm bảo mỗi lần Render khởi động lại, ảnh demo vẫn còn nguyên
COPY uploads /app/uploads

# Mở cổng 8080
EXPOSE 7081

# Câu lệnh chạy web
ENTRYPOINT ["java", "-jar", "app.jar"]