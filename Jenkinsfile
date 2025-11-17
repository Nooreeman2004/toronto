pipeline {
    agent any
    environment {
        PROJECT_NAME = 'toronto_jenkins'
        COMPOSE_FILE = 'docker-compose-jenkins.yml'
    }
    stages {
        stage('Checkout Code') {
            steps {
                script {
                    echo "Starting Git checkout..."
                    git url: 'https://github.com/shazminnasir67/toronto-webapp.git', branch: 'main', credentialsId: 'github-pat'
                    echo "Git checkout completed."
                }
            }
        }
        stage('Build and Deploy Container') {
            steps { 
                sh "docker-compose -p $PROJECT_NAME -f $COMPOSE_FILE up --build -d" 
            } 
        }
    }
    post {
        success {
            echo "Pipeline completed successfully!"
            sh "docker-compose -p ${PROJECT_NAME} -f ${COMPOSE_FILE} logs"
            echo "Web app should be running at http://<ec2-ip>:8082"
        }
        failure {
            echo "Pipeline failed. Check logs for details."
            sh "docker-compose -p ${PROJECT_NAME} -f ${COMPOSE_FILE} logs || true"
            sh "docker ps -a"
            error "Pipeline failed. See console for more details."
        }
        always {
            echo "Capturing final container status..."
            sh "docker ps"
        }
    }
    options {
        timeout(time: 5, unit: 'MINUTES') // Fail after 5 minutes
    }
}