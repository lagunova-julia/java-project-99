FROM eclipse-temurin:20-jdk

ARG GRADLE_VERSION=8.2

# Установка unzip
RUN apt-get update && apt-get install -yq unzip

# Скачивание и установка Gradle
RUN wget -q https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip \
    && unzip gradle-${GRADLE_VERSION}-bin.zip \
    && rm gradle-${GRADLE_VERSION}-bin.zip

# Установка переменной окружения для Gradle
ENV GRADLE_HOME=/opt/gradle

# Перемещение установленного Gradle в указанную директорию
RUN mv gradle-${GRADLE_VERSION} ${GRADLE_HOME}

# Добавление Gradle в PATH
ENV PATH=$PATH:$GRADLE_HOME/bin

# Установка рабочей директории в корень проекта
WORKDIR /app

# Копирование всех файлов из корня проекта в контейнер
COPY . .

# Сборка проекта
RUN gradle installDist

# Команда для запуска приложения
CMD ./build/install/app/bin/app
