#FROM azul/zulu-openjdk-alpine:latest
FROM registry.dev.test:5000/azul/zulu-openjdk-alpine:latest

# --- copy application files
ENV WEB_APP_JAR="ping-1.0.0-SNAPSHOT-fat.jar"
COPY target/${WEB_APP_JAR} .

# --- run the application
CMD java -jar $WEB_APP_JAR
