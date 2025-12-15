pipeline {
    agent any

    environment {
        PROJECT_NAME = 'toronto'
        COMPOSE_FILE = 'docker-compose.yml'
        COMPOSE_PROFILE = 'jenkins'
        APP_URL = 'http://localhost:8083'
    }

    options {
        timestamps()
        ansiColor('xterm')
        timeout(time: 5, unit: 'MINUTES')
    }

    triggers {
        githubPush()
    }

    stages {

        stage('Checkout') {
            steps {
                echo "🔄 Checking out latest code..."
                checkout scm
                sh 'pwd && ls -la'
            }
        }

        stage('Docker Info') {
            steps {
                sh 'docker version'
                sh 'docker compose version'
            }
        }

        stage('Stop Previous Containers') {
            steps {
                echo "🧹 Stopping any running containers..."
                sh "docker compose -p ${PROJECT_NAME} -f ${COMPOSE_FILE} --profile ${COMPOSE_PROFILE} down --remove-orphans || true"
            }
        }

        stage('Setup Firebase Credentials') {
            steps {
                echo "🔑 Setting up Firebase service account..."
                withCredentials([file(credentialsId: 'firebase-service-account', variable: 'FIREBASE_KEY')]) {
                    sh 'cp $FIREBASE_KEY firebase-service-account.json'
                }
            }
        }

        stage('Build and Deploy') {
            steps {
                echo "🚀 Building and starting containers..."
                sh "docker compose -p ${PROJECT_NAME} -f ${COMPOSE_FILE} --profile ${COMPOSE_PROFILE} up -d --build"
            }
        }

        stage('Smoke Test') {
            steps {
                echo "🧪 Checking if app is up..."
                sh '''
                sleep 10
                if curl -I http://localhost:8083 2>/dev/null | grep -q "200"; then
                    echo "✅ Toronto Web App is live!"
                else
                    echo "⚠️ App did not respond as expected."
                fi
                '''
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline completed successfully!"
            sh "docker compose -p ${PROJECT_NAME} -f ${COMPOSE_FILE} --profile ${COMPOSE_PROFILE} logs"
            echo "🌐 Toronto Web App running at http://<ec2-ip>:8083"
        }
        failure {
            echo "❌ Pipeline failed. Check logs for details."
            sh "docker compose -p ${PROJECT_NAME} -f ${COMPOSE_FILE} --profile ${COMPOSE_PROFILE} logs || true"
            sh "docker ps -a"
        }
        always {
            echo "📋 Final container status:"
            sh "docker ps"
        }
    }
}