#!/bin/bash

echo "🚀 Начинаем генерацию структуры проекта SnowSolution..."

# 1. Создаем правильное дерево директорий
mkdir -p eureka-server/src/main/java/com/snow/solution/eureka
mkdir -p eureka-server/src/main/resources

mkdir -p api-gateway/src/main/java/com/snow/solution/gateway
mkdir -p api-gateway/src/main/resources

# 2. Создаем родительский pom.xml в корне
cat << 'XML' > pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.snow.solution</groupId>
    <artifactId>snow-solution-parent</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>snow-solution-parent</name>

    <modules>
        <module>eureka-server</module>
        <module>api-gateway</module>
    </modules>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.8</version>
        <relativePath/>
    </parent>

    <properties>
        <java.version>25</java.version>
        <spring-cloud.version>2025.1.3</spring-cloud.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
XML

# 3. Настраиваем модуль Eureka Server
cat << 'XML' > eureka-server/pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.snow.solution</groupId>
        <artifactId>snow-solution-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>eureka-server</artifactId>
    <name>eureka-server</name>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
XML

cat << 'YAML' > eureka-server/src/main/resources/application.yml
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  instance:
    hostname: ${EUREKA_HOST:localhost}
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
  server:
    enable-self-preservation: false
    eviction-interval-timer-in-ms: 10000
YAML

cat << 'JAVA' > eureka-server/src/main/java/com/snow/solution/eureka/EurekaServerApplication.java
package com.snow.solution.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
JAVA

# 4. Настраиваем модуль API Gateway
cat << 'XML' > api-gateway/pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.snow.solution</groupId>
        <artifactId>snow-solution-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>api-gateway</artifactId>
    <name>api-gateway</name>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
XML

cat << 'YAML' > api-gateway/src/main/resources/application.yml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
      routes:
        - id: bpmn-service-http
          uri: lb://BPMN-SERVICE
          predicates:
            - Path=/api/v1/bpmn/**

        - id: llm-orchestrator-ws
          uri: lb:ws://LLM-ORCHESTRATOR-SERVICE
          predicates:
            - Path=/ws/llm/**

        - id: llm-orchestrator-http
          uri: lb://LLM-ORCHESTRATOR-SERVICE
          predicates:
            - Path=/api/v1/llm/**

        - id: auth-service
          uri: lb://AUTH-SERVICE
          predicates:
            - Path=/api/v1/auth/**

eureka:
  client:
    service-url:
      defaultZone: http://${EUREKA_HOST:localhost}:8761/eureka/
  instance:
    prefer-ip-address: true
YAML

cat << 'JAVA' > api-gateway/src/main/java/com/snow/solution/gateway/ApiGatewayApplication.java
package com.snow.solution.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
JAVA

# 5. Создаем docker-compose.yml
cat << 'YAML' > docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: snow-postgres
    environment:
      POSTGRES_DB: snow_db
      POSTGRES_USER: snow_admin
      POSTGRES_PASSWORD: snow_password_secure
    ports:
      - "5432:5432"
    command: >
      postgres
      -c max_connections=40
      -c shared_buffers=32MB
      -c effective_cache_size=96MB
      -c work_mem=4MB
      -c maintenance_work_mem=16MB
    deploy:
      resources:
        limits:
          memory: 200M
    restart: unless-stopped

  rabbitmq:
    image: rabbitmq:3.13-management-alpine
    container_name: snow-rabbitmq
    environment:
      RABBITMQ_DEFAULT_USER: snow_mq
      RABBITMQ_DEFAULT_PASS: snow_mq_password
    ports:
      - "5672:5672"   # AMQP
      - "15672:15672" # Management Dashboard
    deploy:
      resources:
        limits:
          memory: 256M
    restart: unless-stopped
YAML

# Удаляем ошибочную старую папку main, если она осталась в корне api-gateway
if [ -d "api-gateway/main" ]; then
    rm -rf api-gateway/main
fi

echo "✅ Проект успешно инициализирован!"
