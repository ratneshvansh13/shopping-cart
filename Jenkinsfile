pipeline {
    agent any

    tools {
        maven 'Maven-3.9.6'
    }

    environment {
        DOCKER_IMAGE = "ratneshvansh13/shopping-cart"
        DOCKER_TAG   = "${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'feature',
                    url: 'https://github.com/ratneshvansh13/shopping-cart.git'
            }
        }

        stage('Check Files') {
            steps {
                sh '''
                    echo "======================================"
                    echo "Current workspace:"
                    pwd

                    echo "Workspace files:"
                    ls -la

                    echo "Searching for compose files:"
                    find . -maxdepth 3 -type f \\( \
                        -name "docker-compose.yml" -o \
                        -name "compose.yml" \
                    \\) -print
                    echo "======================================"
                '''
            }
        }

        stage('Maven Build') {
            steps {
                sh '''
                    set -e
                    mvn clean verify
                '''
            }
        }

        stage('SonarQube Scan') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        set -e

                        mvn sonar:sonar \
                            -Dsonar.projectKey=shopping-cart \
                            -Dsonar.host.url="$SONAR_HOST_URL" \
                            -Dsonar.login="$SONAR_AUTH_TOKEN"
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: false
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    set -e

                    echo "Building Docker image:"
                    echo "$DOCKER_IMAGE:$DOCKER_TAG"

                    docker build \
                        -t "$DOCKER_IMAGE:$DOCKER_TAG" .

                    echo "Creating latest tag..."

                    docker tag \
                        "$DOCKER_IMAGE:$DOCKER_TAG" \
                        "$DOCKER_IMAGE:latest"

                    echo "Verifying image..."

                    docker image inspect \
                        "$DOCKER_IMAGE:$DOCKER_TAG"

                    echo "Docker images:"
                    docker images "$DOCKER_IMAGE"
                '''
            }
        }

        stage('Trivy Security Scan') {
            steps {
                sh '''
                    set -e

                    echo "======================================"
                    echo "Starting Trivy Security Scan"
                    echo "Image: $DOCKER_IMAGE:$DOCKER_TAG"
                    echo "======================================"

                    docker run --rm \
                        -v /var/run/docker.sock:/var/run/docker.sock \
                        -v trivy-cache:/root/.cache/ \
                        -v "$WORKSPACE:/work" \
                        aquasec/trivy:latest \
                        image \
                        --scanners vuln \
                        --format table \
                        --output /work/trivy-report.txt \
                        "$DOCKER_IMAGE:$DOCKER_TAG"

                    echo "======================================"
                    echo "Trivy Scan Completed"
                    echo "======================================"

                    cat trivy-report.txt
                '''
            }

            post {
                always {
                    archiveArtifacts(
                        artifacts: 'trivy-report.txt',
                        allowEmptyArchive: true
                    )
                }
            }
        }

        stage('Docker Login & Push') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {
                    sh '''
                        set -e

                        echo "$DOCKER_PASS" | docker login \
                            -u "$DOCKER_USER" \
                            --password-stdin

                        echo "Pushing versioned image..."

                        docker push \
                            "$DOCKER_IMAGE:$DOCKER_TAG"

                        echo "Pushing latest image..."

                        docker push \
                            "$DOCKER_IMAGE:latest"
                    '''
                }
            }
        }

        stage('Deploy to EC2') {
            steps {

                withCredentials([
                    string(
                        credentialsId: 'deployment-server',
                        variable: 'DEPLOYMENT_SERVER'
                    )
                ]) {

                    sshagent(credentials: ['ec2-ssh-key']) {

                        sh '''
                            set -e

                            echo "======================================"
                            echo "Starting EC2 Deployment"
                            echo "======================================"

                            echo "Checking compose file..."

                            if [ -f compose.yml ]; then
                                COMPOSE_FILE="compose.yml"
                            elif [ -f docker-compose.yml ]; then
                                COMPOSE_FILE="docker-compose.yml"
                            else
                                echo "ERROR: No Compose file found!"
                                exit 1
                            fi

                            echo "Using compose file: $COMPOSE_FILE"

                            echo "Creating deployment directory..."

                            ssh -o StrictHostKeyChecking=no \
                                ec2-user@"$DEPLOYMENT_SERVER" \
                                "mkdir -p /home/ec2-user/shopping-cart"

                            echo "Copying Compose file to EC2..."

                            scp -o StrictHostKeyChecking=no \
                                "$COMPOSE_FILE" \
                                ec2-user@"$DEPLOYMENT_SERVER":/home/ec2-user/shopping-cart/compose.yml

                            echo "Deploying application..."

                            ssh -o StrictHostKeyChecking=no \
                                ec2-user@"$DEPLOYMENT_SERVER" \
                                "
                                    set -e

                                    cd /home/ec2-user/shopping-cart

                                    echo 'Current directory:'
                                    pwd

                                    echo 'Compose file:'
                                    ls -lh compose.yml

                                    echo 'Pulling latest Docker images...'
                                    docker compose -f compose.yml pull

                                    echo 'Starting application...'
                                    docker compose -f compose.yml up -d

                                    echo 'Container status:'
                                    docker compose -f compose.yml ps

                                    echo 'Cleaning unused images...'
                                    docker image prune -f
                                "

                            echo "======================================"
                            echo "Deployment completed successfully"
                            echo "======================================"
                        '''
                    }
                }
            }
        }
    }

    post {

        success {
            echo 'Build and Deployment Successful'
        }

        failure {
            echo 'Build or Deployment Failed'
        }

        always {
            sh 'docker logout || true'
        }
    }
}