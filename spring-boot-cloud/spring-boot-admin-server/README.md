# Spring Boot Admin Server

This project provides a **Spring Boot Admin Server** to demonstrate on how it can be used to monitor and manage Spring Boot applications in your environment.

## Features

- Monitoring of Spring Boot client applications
- View application details, metrics, environment info, health, logging levels, etc.
- Manage and view Health, HTTP exchanges, JMX, environment, config props, logs and logging levels and more 
- Notification and alerting options (email, Slack, etc. can be configured)
- Supports service discovery (Eureka, Consul, or simple static instance list)

## Quick Start

1. **Run the Admin Server**

   You can start the server using Maven:

   ```shell
   mvn spring-boot:run
   ```

   Or build and run with Java:

   ```shell
   mvn clean package
   java -jar target/<your-jar-name>.jar
   ```

2. **Access the Admin UI**

   Open your browser and go to:  
   [http://localhost:8090](http://localhost:8090)

3. **Register Client Applications**

   - **With Spring Cloud Discovery:**  
     Client applications will automatically register via your configured discovery system (Eureka, Kubernetes, etc.)

   - **With Simple Spring Cloud Discovery:**
     Add the following dependency to your client application:

     ```xml
     <dependency>
         <groupId>org.springframework.cloud</groupId>
         <artifactId>spring-cloud-starter-bootstrap</artifactId>
     </dependency>
     ```

     > **Note:** The `spring-cloud-starter-bootstrap` dependency is required for Simple Discovery to work properly.

     Configuring `spring.cloud.discovery.client.simple` as in `application.yaml`:

      ```yaml
      spring:
        cloud:
          discovery:
            client:
              simple:
                instances:
                  service1:
                    - uri: http://instance1:8080
                      metadata:
                        management.port: 8081
                  service2:
                    - uri: http://instance2:8080
      ```


   - **With Direct Registration:**  
     Configure the following in the client’s `application.yml`:

     ```yaml
     spring:
       boot:
         admin:
           client:
             url: http://localhost:8090
     ```

## Configuration

Here is an example snippet from `application.yaml` for static service registration:

