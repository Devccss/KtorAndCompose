FROM gradle:8.3-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle installDist

FROM openjdk:17-jdk
WORKDIR /app
COPY --from=builder /app/build/install/backend/app
EXPOSE 8080
CMD ["./bin/backend"]
