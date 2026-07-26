pipeline {
    agent any

    tools {
        maven 'Maven3'
    }

    environment {
        APP_NAME   = "pdp-service"
        IMAGE_NAME = "pdp-service"
        IMAGE_TAG = sh(script: "date +%Y%m%d-%H%M%S", returnStdout: true).trim()
        NAMESPACE  = "default"
        APP_PORT   = "5059"
        HOSTNAME   = "app.pdp.in.bank.com"
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
                    echo "Building Docker image without cache..."

                    # Remove old image with same tag (if exists)
                    docker rmi ${IMAGE_NAME}:${IMAGE_TAG} 2>/dev/null || true

                    # Build with --no-cache and --pull to ensure fresh base image
                    docker build --no-cache --pull -t ${IMAGE_NAME}:${IMAGE_TAG} .

                    # Tag as latest as well (for local development)
                    docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest

                    echo "========================================="
                    echo "Image details:"
                    echo "========================================="
                    # FIXED: Using correct format without .ImageID
                    docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.ID}}\t{{.Size}}\t{{.CreatedAt}}" | grep ${IMAGE_NAME} || true

                    echo ""
                    echo "Image built successfully: ${IMAGE_NAME}:${IMAGE_TAG}"

                    # Verify the image has correct properties
                    echo "========================================="
                    echo "Verifying image properties..."
                    echo "========================================="
                    echo "Properties verification skipped"

                    # Check the image creation time
                    echo ""
                    echo "Image creation time:"
                    docker inspect ${IMAGE_NAME}:${IMAGE_TAG} --format='{{.Created}}'
                '''
            }
        }

        stage('Install Ingress Controller') {
            steps {
                sh '''
                    echo "Checking if Ingress Controller is installed..."
                    if ! kubectl get namespace ingress-nginx &>/dev/null; then
                        echo "Installing NGINX Ingress Controller..."
                        kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.1/deploy/static/provider/cloud/deploy.yaml
                        echo "Waiting for Ingress Controller to be ready..."
                        kubectl wait --namespace ingress-nginx \
                          --for=condition=ready pod \
                          --selector=app.kubernetes.io/component=controller \
                          --timeout=120s
                    else
                        echo "Ingress Controller already installed"
                    fi
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh '''
                    echo "========================================="
                    echo "Deploying to Kubernetes with image: ${IMAGE_NAME}:${IMAGE_TAG}"
                    echo "========================================="

                    # Delete existing resources
                    kubectl delete deployment ${APP_NAME} --ignore-not-found=true
                    kubectl delete service ${APP_NAME} --ignore-not-found=true
                    kubectl delete ingress ${APP_NAME}-ingress --ignore-not-found=true

                    sleep 3

                    # Verify the image exists locally with the specific tag
                    echo "Verifying image exists locally: ${IMAGE_NAME}:${IMAGE_TAG}..."
                    if ! docker images --format '{{.Repository}}:{{.Tag}}' | grep -q "^${IMAGE_NAME}:${IMAGE_TAG}$"; then
                        echo "Image ${IMAGE_NAME}:${IMAGE_TAG} not found!"
                        echo "Available images:"
                        docker images | grep ${IMAGE_NAME}
                        exit 1
                    else
                        echo "Image ${IMAGE_NAME}:${IMAGE_TAG} exists"
                    fi

                    # Also ensure latest tag is updated (for local development)
                    echo "Updating latest tag..."
                    docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest 2>/dev/null || true

                    echo "Creating deployment with version: ${IMAGE_TAG}..."
                    cat <<EOF | kubectl apply -f -
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ${APP_NAME}
  labels:
    app: ${APP_NAME}
    version: ${IMAGE_TAG}
spec:
  replicas: 1
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
        version: ${IMAGE_TAG}
        deployment-date: $(date -u +'%Y-%m-%dT%H:%M:%SZ')
    spec:
      containers:
      - name: ${APP_NAME}
        image: ${IMAGE_NAME}:${IMAGE_TAG}
        imagePullPolicy: Always
        ports:
        - containerPort: ${APP_PORT}
        env:
        - name: SERVER_PORT
          value: "${APP_PORT}"
        - name: APP_VERSION
          value: "${IMAGE_TAG}"
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
          failureThreshold: 3
        readinessProbe:
          tcpSocket:
            port: ${APP_PORT}
          initialDelaySeconds: 45
          periodSeconds: 5
          failureThreshold: 3
---
apiVersion: v1
kind: Service
metadata:
  name: ${APP_NAME}
  labels:
    app: ${APP_NAME}
    version: ${IMAGE_TAG}
spec:
  type: ClusterIP
  ports:
  - port: ${APP_PORT}
    targetPort: ${APP_PORT}
    name: http
  selector:
    app: ${APP_NAME}
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ${APP_NAME}-ingress
  labels:
    app: ${APP_NAME}
    version: ${IMAGE_TAG}
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "false"
spec:
  ingressClassName: nginx
  rules:
  - host: ${HOSTNAME}
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: ${APP_NAME}
            port:
              number: ${APP_PORT}
EOF

                    echo "Waiting for rollout to complete..."
                    kubectl rollout status deployment/${APP_NAME} --timeout=180s

                    echo ""
                    echo "========================================="
                    echo "Deployment Status:"
                    echo "========================================="
                    echo "Pods:"
                    kubectl get pods -l app=${APP_NAME} -o wide
                    echo ""
                    echo "Services:"
                    kubectl get svc ${APP_NAME}
                    echo ""
                    echo "Ingress:"
                    kubectl get ingress ${APP_NAME}-ingress
                    echo ""
                    echo "Images in use:"
                    kubectl get pods -l app=${APP_NAME} -o jsonpath='{.items[*].spec.containers[*].image}'

                    echo ""
                    echo "========================================="
                    echo "Deployment successful!"
                    echo "Image: ${IMAGE_NAME}:${IMAGE_TAG}"
                    echo "Host: ${HOSTNAME}"
                    echo "========================================="
                '''
            }
        }

        stage('Verify Deployment') {
            steps {
                sh '''
                    echo "========================================="
                    echo "Verifying deployment..."
                    echo "========================================="

                    # Get the pod name
                    POD_NAME=$(kubectl get pods -l app=${APP_NAME} -o jsonpath='{.items[0].metadata.name}')

                    if [ -z "$POD_NAME" ]; then
                        echo "ERROR: No pods found!"
                        exit 1
                    fi

                    echo "Pod Name: $POD_NAME"
                    echo ""

                    # Check the image being used
                    echo "Image being used:"
                    kubectl describe pod $POD_NAME | grep "Image:"
                    echo ""

                    # Check the actual image ID
                    echo "Image ID:"
                    kubectl describe pod $POD_NAME | grep "Image ID:"
                    echo ""

                    # Verify the image matches what we built
                    ACTUAL_IMAGE=$(kubectl get pod $POD_NAME -o jsonpath='{.spec.containers[0].image}')
                    EXPECTED_IMAGE="${IMAGE_NAME}:${IMAGE_TAG}"

                    if [ "$ACTUAL_IMAGE" == "$EXPECTED_IMAGE" ]; then
                        echo "Pod is using the correct image: $ACTUAL_IMAGE"
                    else
                        echo "ERROR: Pod is using wrong image!"
                        echo "Expected: $EXPECTED_IMAGE"
                        echo "Actual: $ACTUAL_IMAGE"
                        exit 1
                    fi
                    echo ""

                    # Check logs
                    echo "Recent logs:"
                    kubectl logs $POD_NAME --tail=20 || true
                    echo ""

                    # Verify properties in the running pod
                    echo "Properties in pod:"
                    kubectl exec $POD_NAME -- sh -c "unzip -p /app/app.jar BOOT-INF/classes/application.properties | grep app.host 2>/dev/null || echo 'Properties check not available'"
                    echo ""

                    echo "========================================="
                    echo "Verification successful!"
                    echo "========================================="
                '''
            }
        }

        stage('Smoke Test') {
            steps {
                sh '''
                    echo "Running smoke tests..."

                    echo "Checking pod status..."
                    RUNNING_PODS=$(kubectl get pods -l app=${APP_NAME} --field-selector=status.phase=Running -o name | wc -l | tr -d ' ')

                    if [ "$RUNNING_PODS" = "1" ]; then
                        echo "Pod is running successfully!"
                        echo "Application is deployed and running."

                        echo ""
                        echo "Pod details:"
                        kubectl get pods -l app=${APP_NAME} -o wide

                        echo ""
                        echo "Service details:"
                        kubectl get svc ${APP_NAME}

                        echo ""
                        echo "Ingress details:"
                        kubectl get ingress ${APP_NAME}-ingress

                        echo ""
                        echo "Application URL: http://${HOSTNAME}"
                        echo ""
                        echo "Smoke tests passed!"
                    else
                        echo "ERROR: Not all pods are running"
                        echo "Expected: 1, Running: $RUNNING_PODS"
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
            echo "  Deployment Successful!"
            echo "=========================================="
            echo "Application: ${APP_NAME}"
            echo "Version: ${IMAGE_TAG}"
            echo "Port: ${APP_PORT}"
            echo ""
            echo "Access the application:"
            echo "  http://${HOSTNAME}"
            echo ""
            echo "=========================================="
            echo "Updating /etc/hosts file..."
            echo "=========================================="

            sh '''
                echo "Checking /etc/hosts entry..."

                # Check if hostname already exists in /etc/hosts
                if grep -q "${HOSTNAME}" /etc/hosts; then
                    echo "Hostname ${HOSTNAME} already exists in /etc/hosts"
                else
                    echo "Attempting to add ${HOSTNAME} to /etc/hosts..."

                    # Try without sudo first (if running as root)
                    if echo "127.0.0.1 ${HOSTNAME}" >> /etc/hosts 2>/dev/null; then
                        echo "Hostname added successfully without sudo!"
                    else
                        echo "Need sudo to add hostname..."
                        echo "Please manually add this entry to /etc/hosts:"
                        echo "  127.0.0.1 ${HOSTNAME}"
                        echo ""
                        echo "Or run this command in terminal:"
                        echo "  sudo sh -c 'echo \"127.0.0.1 ${HOSTNAME}\" >> /etc/hosts'"
                    fi
                fi

                echo ""
                echo "Verifying /etc/hosts entry:"
                grep "${HOSTNAME}" /etc/hosts || echo "Hostname not found in /etc/hosts"

                echo ""
                echo "=========================================="
                echo "Application is accessible at:"
                echo "  http://${HOSTNAME}"
                echo ""
                echo "If you can't access, add to /etc/hosts:"
                echo "  sudo sh -c 'echo \"127.0.0.1 ${HOSTNAME}\" >> /etc/hosts'"
                echo "=========================================="
            '''
        }

        failure {
            echo "=========================================="
            echo "  Deployment Failed!"
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

                echo "Ingress Status:"
                kubectl describe ingress ${APP_NAME}-ingress || echo "Ingress not found"

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