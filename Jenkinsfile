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
            echo "Current workspace:"
            pwd

            echo "Workspace files:"
            ls -la

            echo "Searching for compose files:"
            find . -maxdepth 3 -type f \\( \
                -name "docker-compose.yml" -o \
                -name "compose.yml" \
            \\) -print
            '''
        }
        }

        stage('Maven Build') {
            steps {
                sh 'mvn clean verify'
            }
        }

        stage('SonarQube Scan') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
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
                    docker build \
                        -t "$DOCKER_IMAGE:$DOCKER_TAG" .

                    docker tag \
                        "$DOCKER_IMAGE:$DOCKER_TAG" \
                        "$DOCKER_IMAGE:$DOCKER_TAG"
                '''
            }
        }
        stage('Trivy Security Scan') {
            steps {
            sh '''
                docker run --rm \
                -v /var/run/docker.sock:/var/run/docker.sock \
                -v trivy-cache:/root/.cache/ \
                -v "$PWD:/work" \
                aquasec/trivy:latest \
                image \
                --format table \
                --output /work/trivy-report.txt \
                ${DOCKER_IMAGE}:${DOCKER_TAG}
            '''
        }

        post {
            always {
                archiveArtifacts artifacts: 'trivy-report.txt',
                allowEmptyArchive: true
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
                        echo "$DOCKER_PASS" | docker login \
                            -u "$DOCKER_USER" \
                            --password-stdin

                        docker push "$DOCKER_IMAGE:$DOCKER_TAG"
                        docker push "$DOCKER_IMAGE:latest"
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

                    echo "======================================"
                    echo "Starting EC2 Deployment"
                    echo "======================================"

                    echo "Checking compose file..."

                    test -f compose.yml

                    echo "Deploying application..."

                    ssh -o StrictHostKeyChecking=no \
                        ec2-user@"$DEPLOYMENT_SERVER" \
                        "cd /home/ec2-user/shopping-cart && \
                         docker compose -f compose.yml pull && \
                         docker compose -f compose.yml up -d && \
                         docker compose -f compose.yml ps"

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