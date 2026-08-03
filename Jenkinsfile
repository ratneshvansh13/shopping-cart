pipeline{
    agent any

    tools{
        maven "Maven-3.9.6"
        // jdk  'JDK17'
    }

    environment{
        APP_NAME = "shopping-cart"
    }

    stages{
        
        stage('Checkout'){
            steps{
                git branch: 'devops', url: 'https://github.com/ratneshvansh13/DevOps-Documents.git'
            }
        }
        stage('Maven Build'){
            steps{
                sh 'mvn clean package'
            }
        }
    }

    post{
        success{
            echo "Build Successful"
        }
        failure{
            echo "Build Failed"
        }
    }
}