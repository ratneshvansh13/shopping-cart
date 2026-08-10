pipeline {
    agent any

    tools {
        maven 'Maven-3.9.6'
    }

    environment {
        DOCKER_IMAGE = "ratneshvansh13/shopping-cart"
        DOCKER_TAG = "${BUILD_NUMBER}"
        DEPLOYMENT_SERVER = "172.31.39.245"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'devops-test',
                    url: 'https://github.com/ratneshvansh13/shopping-cart.git'
            }
        }

        stage('Maven Build') {
            steps {
                sh 'mvn clean verify -DskipTests'
            }
        }

        stage('SonarQube Scan') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn sonar:sonar \
                          -Dsonar.projectKey=shopping-cart
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                sh "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"
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

                        docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                        docker push ${DOCKER_IMAGE}:latest
                    '''
                }
            }
        }

        stage('Deploy to EC2') {
            steps {
                sshagent(['ec2-ssh-key']) {
                    sh '''
                        echo "Deploying to $DEPLOYMENT_SERVER"

                        ssh -o StrictHostKeyChecking=no \
                            root@$DEPLOYMENT_SERVER << 'EOF'

                        docker pull ratneshvansh13/shopping-cart:latest

                        docker stop shopping-cart || true
                        docker rm shopping-cart || true

                        docker run -d \
                            --name shopping-cart \
                            --restart unless-stopped \
                            -p 8080:8080 \
                            ratneshvansh13/shopping-cart:latest

                        docker image prune -f

                        EOF
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'Build Successful'
        }

        failure {
            echo 'Build Failed'
        }

        always {
            sh 'docker logout || true'
        }
    }
}
