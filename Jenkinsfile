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

        stage('Deploy') {
            steps {
                withCredentials([
                    file(
                    credentialsId: 'shopping-cart-properties',
                    variable: 'APP_PROPERTIES'
            )
        ]) {
                    sh '''
                        scp "$APP_PROPERTIES" ec2-user@$DEPLOYMENT_SERVER:/tmp/application.properties

                        ssh ec2-user@$DEPLOYMENT_SERVER "
                        sudo mkdir -p /opt/shopping-cart &&
                        sudo mv /tmp/application.properties /opt/shopping-cart/application.properties &&
                        sudo chmod 600 /opt/shopping-cart/application.properties
                "
            '''
        }

                sshagent(credentials: ['ec2-ssh-key']) {
                    sh '''
                        ssh ec2-user@$DEPLOYMENT_SERVER "
                        docker pull ratneshvansh13/shopping-cart:latest &&
                        docker stop shopping-cart || true &&
                        docker rm shopping-cart || true &&
                        docker run -d \
                            --name shopping-cart \
                            -p 8080:8080 \
                            -v /opt/shopping-cart/application.properties:/usr/local/tomcat/webapps/ROOT/WEB-INF/classes/application.properties:ro \
                            ratneshvansh13/shopping-cart:latest
                    "
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
