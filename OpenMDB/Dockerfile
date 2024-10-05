FROM openjdk:17-jdk-alpine

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем JAR файл в контейнер (предполагается, что JAR уже собран)
COPY target/OpenMDB-0.0.1-SNAPSHOT.jar /app/OpenMDB-0.0.1-SNAPSHOT.jar

# Открываем порт, который использует сервис (замените на нужный)
EXPOSE 8083

# Определяем команду для запуска приложения
ENTRYPOINT ["java", "-jar", "OpenMDB-0.0.1-SNAPSHOT.jar"]