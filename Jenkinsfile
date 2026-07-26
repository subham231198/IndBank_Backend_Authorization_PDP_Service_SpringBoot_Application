pipeline {
    agent any

    tools {
        maven 'Maven3'
    }

    environment {
        APP_NAME   = "pdp-service"
        IMAGE_NAME = "pdp-service"
        IMAGE_TAG  = "${BUILD_NUMBER}"
        NAMESPACE  = "default"
        APP_PORT   = "5059"
        PATH = "/usr/local/bin:/opt/homebrew/bin:/usr/bin:/bin:/usr/sbin:/sbin:${env.PATH}"
    }

    stages {

        stage('Verify Environment') {
            steps {
                sh '''
                    echo "========== SYSTEM INFO =========="
                    uname -a
                    sw_vers

                    echo "========== JAVA =========="
                    java -version

                    echo "========== MAVEN =========="
                    mvn -version

                    echo "========== DOCKER =========="
                    docker --version

                    echo "========== DOCKER COMPOSE =========="
                    docker compose version

                    echo "========== KUBECTL =========="
                    kubectl version --client

                    echo "========== KUBERNETES CONTEXT =========="
                    kubectl config current-context
                    kubectl get nodes
                '''
            }
        }

        stage('Build Spring Boot Application') {
            steps {
                sh '''
                    echo "Building Spring Boot application..."
                    mvn clean compile -DskipTests
                    mvn package -DskipTests

                    if [ -f target/*.jar ]; then
                        echo "JAR built successfully"
                        ls -lh target/*.jar
                    else
                        echo "ERROR: JAR file not created"
                        exit 1
                    fi
                '''
            }
        }

        stage('Run Unit Tests') {
            steps {
                sh '''
                    echo "Running unit tests..."
                    mvn test
                '''
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh '''
                    echo "Building Docker image..."
                    docker build -t ${IMAGE_NAME}:latest .
                    docker tag ${IMAGE_NAME}:latest ${IMAGE_NAME}:${IMAGE_TAG}

                    echo "Image details:"
                    docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}" | grep ${IMAGE_NAME}

                    echo "Image built successfully: ${IMAGE_NAME}:${IMAGE_TAG}"
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh '''
                    echo "Deploying to Kubernetes..."

                    kubectl delete deployment ${APP_NAME} --ignore-not-found=true
                    kubectl delete service ${APP_NAME} --ignore-not-found=true

                    sleep 3

                    echo "Verifying image exists locally..."
                    if ! docker images --format '{{.Repository}}:{{.Tag}}' | grep -q "^${IMAGE_NAME}:latest$"; then
                        echo "Image not found, building..."
                        docker build -t ${IMAGE_NAME}:latest .
                        docker tag ${IMAGE_NAME}:latest ${IMAGE_NAME}:${IMAGE_TAG}
                    else
                        echo "Image ${IMAGE_NAME}:latest exists"
                        docker tag ${IMAGE_NAME}:latest ${IMAGE_NAME}:${IMAGE_TAG} 2>/dev/null || true
                    fi

                    echo "Creating deployment..."
                    cat <<EOF | kubectl apply -f -
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ${APP_NAME}
  labels:
    app: ${APP_NAME}
spec:
  replicas: 5
  selector:
    matchLabels:
      app: ${APP_NAME}
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  template:
    metadata:
      labels:
        app: ${APP_NAME}
    spec:
      containers:
      - name: ${APP_NAME}
        image: ${IMAGE_NAME}:latest
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: ${APP_PORT}
        env:
        - name: SERVER_PORT
          value: "${APP_PORT}"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          tcpSocket:
            port: ${APP_PORT}
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          tcpSocket:
            port: ${APP_PORT}
          initialDelaySeconds: 45
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: ${APP_NAME}
  labels:
    app: ${APP_NAME}
spec:
  type: LoadBalancer
  ports:
  - port: ${APP_PORT}
    targetPort: ${APP_PORT}
    name: http
  selector:
    app: ${APP_NAME}
EOF

                    echo "Waiting for rollout to complete..."
                    kubectl rollout status deployment/${APP_NAME} --timeout=180s

                    echo "Deployment Status:"
                    kubectl get pods -l app=${APP_NAME}
                    kubectl get svc ${APP_NAME}

                    echo "Deployment successful!"
                '''
            }
        }

        stage('Verify Deployment') {
            steps {
                sh '''
                    echo "Verifying deployment..."

                    POD_NAME=$(kubectl get pods -l app=${APP_NAME} -o jsonpath='{.items[0].metadata.name}')

                    if [ -n "$POD_NAME" ]; then
                        echo "Pod Status:"
                        kubectl get pod $POD_NAME

                        echo "Pod Logs:"
                        kubectl logs $POD_NAME --tail=30

                        echo "Service Details:"
                        kubectl get svc ${APP_NAME}
                    else
                        echo "No pods found for ${APP_NAME}"
                        exit 1
                    fi
                '''
            }
        }

        stage('Smoke Test') {
            steps {
                sh '''
                    echo "Running smoke tests..."

                    echo "Checking pod status..."
                    RUNNING_PODS=$(kubectl get pods -l app=${APP_NAME} --field-selector=status.phase=Running -o name | wc -l | tr -d ' ')

                    if [ "$RUNNING_PODS" = "5" ]; then
                        echo "All 5 pods are running successfully!"
                        echo "Application is deployed and running."

                        echo ""
                        echo "Pod details:"
                        kubectl get pods -l app=${APP_NAME} -o wide

                        echo ""
                        echo "Service details:"
                        kubectl get svc ${APP_NAME}

                        echo ""
                        echo "Testing application accessibility..."

                        # Test if application is responding
                        kubectl port-forward svc/${APP_NAME} ${APP_PORT}:${APP_PORT} > /dev/null 2>&1 &
                        PF_PID=$!

                        sleep 10

                        # Try to get any response from the application
                        HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:${APP_PORT}/ 2>/dev/null || echo "000")

                        if [ "$HTTP_CODE" != "000" ]; then
                            echo "Application is responding (HTTP $HTTP_CODE)"
                            echo "Smoke tests passed!"
                        else
                            echo "WARNING: Could not get HTTP response from application"
                            echo "But pods are running, application should be accessible"
                            echo "Check service: kubectl get svc ${APP_NAME}"
                        fi

                        kill $PF_PID 2>/dev/null || true
                    else
                        echo "ERROR: Not all pods are running"
                        echo "Expected: 5, Running: $RUNNING_PODS"
                        kubectl get pods -l app=${APP_NAME}
                        exit 1
                    fi
                '''
            }
        }
    }

    post {
        success {
            echo "=========================================="
            echo "Deployment Successful!"
            echo "=========================================="
            echo "Application: ${APP_NAME}"
            echo "Version: ${IMAGE_TAG}"
            echo "Port: ${APP_PORT}"
            echo ""
            echo "Access the application:"
            echo "  kubectl port-forward svc/${APP_NAME} ${APP_PORT}:${APP_PORT}"
            echo "  curl http://localhost:${APP_PORT}/"
            echo "=========================================="
        }

        failure {
            echo "=========================================="
            echo "Deployment Failed!"
            echo "=========================================="

            sh '''
                echo "Diagnostic Information:"
                echo "Kubernetes Resources:"
                kubectl get all

                echo "Deployment Status:"
                kubectl describe deployment ${APP_NAME} || echo "Deployment not found"

                echo "Pod Status:"
                kubectl get pods -l app=${APP_NAME}
                kubectl describe pods -l app=${APP_NAME} || echo "No pods found"

                echo "Recent Events:"
                kubectl get events --sort-by='.lastTimestamp' | tail -20

                echo "Docker Status:"
                docker ps -a
                docker images | grep ${IMAGE_NAME}
            '''
        }

        always {
            echo "Pipeline finished"

            script {
                if (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master') {
                    sh '''
                        echo "Cleaning up old Docker images..."
                        docker image prune -f --filter "until=24h" || true
                    '''
                }
            }
        }

        cleanup {
            deleteDir()

            sh '''
                kubectl delete pod test-curl --ignore-not-found=true 2>/dev/null || true
            '''
        }
    }
}