FROM maven:3.9.11-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=prod
ENV MENTORCORE_UPLOADS_PATH=/data/uploads
ENV JAVA_OPTS=""

RUN mkdir -p /data/uploads \
    && addgroup --system mentorcore \
    && adduser --system --ingroup mentorcore mentorcore \
    && chown -R mentorcore:mentorcore /app /data/uploads

COPY --from=builder /app/target/*.jar /app/mentorcore.jar

USER mentorcore

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/mentorcore.jar"]
