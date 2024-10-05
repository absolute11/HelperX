FROM openjdk:17-jdk-alpine

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем JAR файл в контейнер (предполагается, что JAR уже собран)
COPY target/foodApi-0.0.1-SNAPSHOT.jar /app/foodApi-0.0.1-SNAPSHOT.jar

# Открываем порт, который использует сервис (замените на нужный)
EXPOSE 8086

# Определяем команду для запуска приложения
ENTRYPOINT ["java", "-jar", "foodApi-0.0.1-SNAPSHOT.jar"]