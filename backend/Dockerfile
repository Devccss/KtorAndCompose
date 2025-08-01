FROM gradle:7.6.1-jdk17 AS builder

COPY . /app
WORKDIR /app

RUN chmod +x ./gradlew
RUN ./gradlew installDist

FROM openjdk:17-jdk-slim
WORKDIR /app


RUN apt-get update && apt-get install -y findutils

COPY --from=builder /app/build/install/backend .

EXPOSE 8080

CMD ["./bin/backend"]
