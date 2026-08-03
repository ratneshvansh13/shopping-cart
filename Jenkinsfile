pipeline {
    agent any

    tools {
        maven 'Maven-3.9.6'
        // jdk 'JDK17'
    }

    environment {
        APP_NAME = "shopping-cart"
        DOCKER_IMAGE = "ratneshvansh13/shopping-cart"
        DOCKER_TAG = "${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'devops',
                    url: 'https://github.com/ratneshvansh13/DevOps-Documents.git'
            }
        }

        stage('Maven Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
         stage('Docker Build') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${BUILD_NUMBER} ."
                sh "docker tag ${IMAGE_NAME}:${BUILD_NUMBER} ${IMAGE_NAME}:latest"
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
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push ${IMAGE_NAME}:${BUILD_NUMBER}
                        docker push ${IMAGE_NAME}:latest
                    '''
                }
            }
        }


    post {
        success {
            echo "Build Successful"
        }
        failure {
            echo "Build Failed"
        }
        always {
            sh 'docker logout || true'
        }
    }
}
}
