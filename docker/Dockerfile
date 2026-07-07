FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

ARG MODULE
ARG JAR_FILE

COPY docker/maven-settings.xml /root/.m2/settings.xml
COPY . .

RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -B -ntp -s /root/.m2/settings.xml -pl "${MODULE}" -am package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

ARG JAR_FILE
ARG APP_JAR=app.jar

ENV APP_ARGS="" \
    APP_JAR="${APP_JAR}" \
    JAVA_TOOL_OPTIONS="-Xms128m -Xmx256m" \
    TZ=Asia/Shanghai

COPY --from=build /workspace/${JAR_FILE} /app/${APP_JAR}

ENTRYPOINT ["sh", "-c", "java -jar /app/${APP_JAR} ${APP_ARGS}"]
