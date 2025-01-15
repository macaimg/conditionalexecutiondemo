# Spring Boot Application with Kubernetes Setup

This guide provides step-by-step instructions to set up, build, and deploy a Spring Boot application with two REST controllers (`TypeOneController` and `TypeTwoController`) in a Kubernetes cluster using Docker Desktop on macOS (ARM64).

---

## **Application Overview**
The application has two REST controllers:
- `TypeOneController`: Accessible via `/typeone/*` endpoints.
- `TypeTwoController`: Accessible via `/typetwo/*` endpoints.

Each controller is deployed as a separate service in the Kubernetes cluster.

---

## **Pre-requisites**
Ensure the following are installed and configured:

1. **Docker Desktop**:
   - Download and install Docker Desktop for macOS from [here](https://www.docker.com/products/docker-desktop/).
   - Enable Kubernetes from Docker Desktop settings:
     1. Open Docker Desktop.
     2. Go to **Settings > Kubernetes**.
     3. Check the box for "Enable Kubernetes" and apply changes.

2. **kubectl**:
   - `kubectl` is included with Docker Desktop. Verify installation:
     ```bash
     kubectl version --client
     ```

3. **Java and Maven**:
   - Java JDK 17 or higher.
   - Apache Maven for building the project.
   - Verify installation:
     ```bash
     java -version
     mvn -version
     ```

4. **Text Editor or IDE**:
   - Use an IDE like IntelliJ IDEA or Visual Studio Code.

---

## **Project Structure**
```plaintext
springboot-demo/
├── Dockerfile
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/example/springbootdemo/
│   │   │   ├── TypeOneController.java
│   │   │   ├── TypeTwoController.java
│   │   │   └── SpringbootDemoApplication.java
│   │   └── resources/
│   │       ├── application-one.yaml
│   │       ├── application-two.yaml
│   │       └── application.yaml
├── k8s/
│   ├── deployment-typeone.yaml
│   ├── deployment-typetwo.yaml
│   ├── service-typeone.yaml
│   ├── service-typetwo.yaml
│   └── ingress.yaml
```

---

## **Setup and Run**

### **1. Build the Application**
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd springboot-demo
   ```

2. Build the application JARs:
   ```bash
   mvn clean package
   ```

---

### **2. Build Docker Images**
Create separate Docker images for `TypeOne` and `TypeTwo` applications:

1. **Dockerfile**:
   ```dockerfile
   FROM openjdk:17-jdk-slim
   ARG JAR_FILE=target/conditionalexecutiondemo-0.0.1-SNAPSHOT.jar
   COPY ${JAR_FILE} app.jar
   ENTRYPOINT ["java", "-jar", "/app.jar"]
   ```

2. Build Docker images:
   ```bash
   docker build -t typeone-app:1.0 --build-arg JAR_FILE=target/conditionalexecutiondemo-0.0.1-SNAPSHOT.jar .
   docker build -t typetwo-app:1.0 --build-arg JAR_FILE=target/conditionalexecutiondemo-0.0.1-SNAPSHOT.jar .
   ```

3. Verify Docker images:
   ```bash
   docker images
   ```

---

### **3. Deploy to Kubernetes**

1. **Create Kubernetes Deployment and Service Files**:
   - `k8s/deployment-typeone.yaml`:
     ```yaml
     apiVersion: apps/v1
     kind: Deployment
     metadata:
       name: typeone-deployment
       labels:
         app: typeone
     spec:
       replicas: 1
       selector:
         matchLabels:
           app: typeone
       template:
         metadata:
           labels:
             app: typeone
         spec:
           containers:
           - name: typeone-container
             image: typeone-app:1.0
             env:
             - name: SPRING_PROFILES_ACTIVE
               value: "typetwo"
             ports:
             - containerPort: 8080
     ```

   - `k8s/service-typeone.yaml`:
     ```yaml
     apiVersion: v1
     kind: Service
     metadata:
       name: typeone-service
     spec:
       selector:
         app: typeone
       ports:
         - protocol: TCP
           port: 80
           targetPort: 8080
       type: ClusterIP
     ```

   - Repeat similar steps for `TypeTwo`.

   - `k8s/ingress.yaml`:
        ```yaml
        apiVersion: networking.k8s.io/v1
        kind: Ingress
        metadata:
            name: demo-app-ingress
            annotations:
                nginx.ingress.kubernetes.io/rewrite-target: ""
        spec:
            ingressClassName: nginx
            rules:
            - host: typeone.local
                http:
                    paths:
                    - path: /typeone
                        pathType: Prefix
                        backend:
                            service:
                                name: typeone-service
                                port:
                                    number: 80
            - host: typetwo.local
                http:
                    paths:
                    - path: /typetwo
                        pathType: Prefix
                        backend:
                            service:
                                name: typetwo-service
                                port:
                                    number: 80
        ```
   

2. **Apply Kubernetes Resources**:
   Deploy the services to the Kubernetes cluster:
   ```bash
   kubectl apply -f k8s/deployment-typeone.yaml
   kubectl apply -f k8s/deployment-typetwo.yaml
   kubectl apply -f k8s/service-typeone.yaml
   kubectl apply -f k8s/service-typetwo.yaml
   ```

3. **Verify Deployments and Services**:
   ```bash
   kubectl get pods
   kubectl get services
   ```

---

### **4. Test the Application**

#### **Option 1: Port Forwarding**
Expose the services locally using port forwarding:

- TypeOne:
  ```bash
  kubectl port-forward service/typeone-service 8080:80
  ```
  Test with:
  ```bash
  curl http://localhost:8080/typeone/message
  ```

- TypeTwo:
  ```bash
  kubectl port-forward service/typetwo-service 8080:80
  ```
  Test with:
  ```bash
  curl http://localhost:8080/typetwo/message
  ```

#### **Option 2: Use Test Pod**
1. Create a test pod:
   ```bash
   kubectl run test-pod --image=alpine --command -- sleep 3600
   ```

2. Access the pod shell:
   ```bash
   kubectl exec -it test-pod -- sh
   ```

3. Test service connectivity:
   ```sh
   wget -qO- http://typeone-service/typeone/message
   wget -qO- http://typetwo-service/typetwo/message
   ```

#### **Option 3: Use Ingress**
If using an ingress controller, add the following to `/etc/hosts`:
```plaintext
127.0.0.1 typeone.local
127.0.0.1 typetwo.local
```
Access the services in a browser or via `curl`:
- `http://typeone.local/typeone/message`
- `http://typetwo.local/typetwo/message`

---

### **5. Clean Up**
To delete the Kubernetes resources:
```bash
kubectl delete deployment typeone-deployment typetwo-deployment
kubectl delete service typeone-service typetwo-service
kubectl delete pod test-pod
```

---

## **Troubleshooting**

- **Pods not starting**: Check logs:
  ```bash
  kubectl logs <pod-name>
  ```

- **Service not reachable**: Verify endpoints:
  ```bash
  kubectl describe service <service-name>
  ```

- **Ingress not working**: Ensure ingress is enabled in Docker Desktop and configured correctly.
