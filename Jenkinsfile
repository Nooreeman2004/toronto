pipeline {
    agent any

    environment {
        PROJECT_NAME = 'toronto'
        COMPOSE_FILE = 'docker-compose.yml'
        APP_URL = 'http://localhost:8083'
    }

    options {
        timestamps()
        ansiColor('xterm')
        timeout(time: 15, unit: 'MINUTES')
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
                script {
                    // Get committer email for notifications
                    env.GIT_COMMITTER_EMAIL = sh(
                        script: "git log -1 --pretty=format:'%ae'",
                        returnStdout: true
                    ).trim()
                    env.GIT_COMMITTER_NAME = sh(
                        script: "git log -1 --pretty=format:'%an'",
                        returnStdout: true
                    ).trim()
                    env.GIT_COMMIT_MSG = sh(
                        script: "git log -1 --pretty=format:'%s'",
                        returnStdout: true
                    ).trim()
                    echo "📧 Committer: ${env.GIT_COMMITTER_NAME} <${env.GIT_COMMITTER_EMAIL}>"
                }
            }
        }

        stage('Docker Info') {
            steps {
                sh 'docker version'
                sh 'docker compose version'
            }
        }

        stage('Cleanup') {
            steps {
                echo "🧹 Stopping any running containers..."
                sh "docker compose -p ${PROJECT_NAME} -f ${COMPOSE_FILE} --profile test down --remove-orphans || true"
                sh "docker compose -p ${PROJECT_NAME} -f ${COMPOSE_FILE} --profile jenkins down --remove-orphans || true"
                sh "docker rm -f toronto_web_dev toronto_test_runner 2>/dev/null || true"
            }
        }

        stage('Setup Firebase Credentials') {
            steps {
                echo "🔑 Setting up Firebase service account..."
                withCredentials([file(credentialsId: 'firebase-service-account', variable: 'FIREBASE_KEY')]) {
                    sh 'cp $FIREBASE_KEY firebase-service-account.json'
                    sh 'ls -la firebase-service-account.json'
                }
            }
        }

        stage('Build and Deploy App') {
            steps {
                echo "🚀 Building and starting web application..."
                sh "docker compose -p ${PROJECT_NAME} -f ${COMPOSE_FILE} --profile jenkins up -d --build"
                echo "⏳ Waiting for app to start..."
                sh 'sleep 20'
                sh 'docker ps'
                sh 'curl -I http://localhost:8083 || echo "App not responding yet..."'
            }
        }

        stage('Run Selenium Tests') {
            steps {
                echo "🧪 Running Java Selenium tests with Maven..."
                sh 'mkdir -p test-reports'
                sh "docker compose -p ${PROJECT_NAME} -f ${COMPOSE_FILE} --profile test up --build --abort-on-container-exit test-runner || true"
                sh 'ls -la test-reports/ || echo "No test reports found"'
            }
            post {
                always {
                    // Archive test reports
                    archiveArtifacts artifacts: 'test-reports/**/*', allowEmptyArchive: true
                    // Publish JUnit test results
                    junit allowEmptyResults: true, testResults: 'test-reports/*.xml'
                }
            }
        }

        stage('Verify App Running') {
            steps {
                echo "🧪 Final health check..."
                sh '''
                echo "Checking app status..."
                docker ps
                sleep 5
                if curl -s -o /dev/null -w "%{http_code}" http://localhost:8083 | grep -q "200"; then
                    echo "✅ Toronto Web App is live and responding!"
                else
                    echo "⚠️ App health check - checking logs..."
                    docker logs toronto_web_dev 2>&1 | tail -50
                fi
                '''
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline completed successfully!"
            echo "🌐 Toronto Web App running at http://<ec2-ip>:8083"
        }
        failure {
            echo "❌ Pipeline failed. Check logs for details."
            sh "docker compose -p ${PROJECT_NAME} -f ${COMPOSE_FILE} --profile jenkins logs || true"
            sh "docker ps -a"
        }
        always {
            echo "📋 Final container status:"
            sh "docker ps"
        }
    }
}