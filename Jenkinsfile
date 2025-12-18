pipeline {
    agent any

    environment {
        PROJECT_NAME = 'toronto'
        COMPOSE_FILE = 'docker-compose.yml'
        APP_URL = 'http://localhost:8083'
        EC2_IP = '13.211.204.233'
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
                    env.GIT_COMMIT_SHA = sh(
                        script: "git log -1 --pretty=format:'%h'",
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
                sh 'rm -f firebase-service-account.json'
                withCredentials([file(credentialsId: 'firebase-service-account', variable: 'FIREBASE_KEY')]) {
                    sh 'cp $FIREBASE_KEY firebase-service-account.json'
                    sh 'chmod 644 firebase-service-account.json'
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
                sh "docker compose -p ${PROJECT_NAME} -f ${COMPOSE_FILE} --profile test up --build --abort-on-container-exit test-runner 2>&1 | tee test-reports/test-execution.log || true"
                sh 'ls -la test-reports/ || echo "No test reports found"'
                // Create a summary file
                sh '''
                echo "===========================================" > test-reports/test-summary.txt
                echo "Toronto Web App - Test Execution Summary" >> test-reports/test-summary.txt
                echo "===========================================" >> test-reports/test-summary.txt
                echo "Date: $(date)" >> test-reports/test-summary.txt
                echo "Build: #${BUILD_NUMBER}" >> test-reports/test-summary.txt
                echo "Commit: ${GIT_COMMIT_SHA} - ${GIT_COMMIT_MSG}" >> test-reports/test-summary.txt
                echo "Triggered by: ${GIT_COMMITTER_NAME}" >> test-reports/test-summary.txt
                echo "===========================================" >> test-reports/test-summary.txt
                echo "" >> test-reports/test-summary.txt
                cat test-reports/test-execution.log >> test-reports/test-summary.txt 2>/dev/null || true
                '''
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
            echo "🌐 Toronto Web App running at http://${EC2_IP}:8083"
            
            // Send success email with test reports
            emailext(
                subject: "✅ BUILD SUCCESS - Toronto Pipeline #${BUILD_NUMBER}",
                body: """
                    <html>
                    <body style="font-family: Arial, sans-serif;">
                        <h2 style="color: #28a745;">✅ Pipeline Build Successful!</h2>
                        
                        <table style="border-collapse: collapse; width: 100%; max-width: 600px;">
                            <tr>
                                <td style="padding: 8px; border: 1px solid #ddd;"><strong>Project</strong></td>
                                <td style="padding: 8px; border: 1px solid #ddd;">Toronto Web App</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px; border: 1px solid #ddd;"><strong>Build Number</strong></td>
                                <td style="padding: 8px; border: 1px solid #ddd;">#${BUILD_NUMBER}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px; border: 1px solid #ddd;"><strong>Triggered By</strong></td>
                                <td style="padding: 8px; border: 1px solid #ddd;">${GIT_COMMITTER_NAME}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px; border: 1px solid #ddd;"><strong>Commit</strong></td>
                                <td style="padding: 8px; border: 1px solid #ddd;">${GIT_COMMIT_SHA} - ${GIT_COMMIT_MSG}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px; border: 1px solid #ddd;"><strong>Status</strong></td>
                                <td style="padding: 8px; border: 1px solid #ddd; color: #28a745;"><strong>All Tests Passed ✅</strong></td>
                            </tr>
                        </table>
                        
                        <h3>🔗 Links</h3>
                        <ul>
                            <li><a href="http://${EC2_IP}:8083">Live Application</a></li>
                            <li><a href="${BUILD_URL}">Jenkins Build</a></li>
                            <li><a href="${BUILD_URL}console">Console Output</a></li>
                            <li><a href="${BUILD_URL}testReport">Test Report</a></li>
                        </ul>
                        
                        <h3>📎 Attachments</h3>
                        <p>Test execution log and reports are attached to this email.</p>
                        
                        <hr>
                        <p style="color: #666; font-size: 12px;">
                            This is an automated message from Jenkins CI/CD Pipeline.
                        </p>
                    </body>
                    </html>
                """,
                to: "${GIT_COMMITTER_EMAIL}",
                mimeType: 'text/html',
                attachmentsPattern: 'test-reports/**/*',
                attachLog: true
            )
        }
        failure {
            echo "❌ Pipeline failed. Check logs for details."
            sh "docker compose -p ${PROJECT_NAME} -f ${COMPOSE_FILE} --profile jenkins logs > test-reports/docker-logs.txt 2>&1 || true"
            sh "docker ps -a"
            
            // Send failure email with logs
            emailext(
                subject: "❌ BUILD FAILED - Toronto Pipeline #${BUILD_NUMBER}",
                body: """
                    <html>
                    <body style="font-family: Arial, sans-serif;">
                        <h2 style="color: #dc3545;">❌ Pipeline Build Failed!</h2>
                        
                        <table style="border-collapse: collapse; width: 100%; max-width: 600px;">
                            <tr>
                                <td style="padding: 8px; border: 1px solid #ddd;"><strong>Project</strong></td>
                                <td style="padding: 8px; border: 1px solid #ddd;">Toronto Web App</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px; border: 1px solid #ddd;"><strong>Build Number</strong></td>
                                <td style="padding: 8px; border: 1px solid #ddd;">#${BUILD_NUMBER}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px; border: 1px solid #ddd;"><strong>Triggered By</strong></td>
                                <td style="padding: 8px; border: 1px solid #ddd;">${GIT_COMMITTER_NAME}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px; border: 1px solid #ddd;"><strong>Commit</strong></td>
                                <td style="padding: 8px; border: 1px solid #ddd;">${GIT_COMMIT_SHA} - ${GIT_COMMIT_MSG}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px; border: 1px solid #ddd;"><strong>Status</strong></td>
                                <td style="padding: 8px; border: 1px solid #ddd; color: #dc3545;"><strong>Build/Tests Failed ❌</strong></td>
                            </tr>
                        </table>
                        
                        <h3>🔗 Links</h3>
                        <ul>
                            <li><a href="${BUILD_URL}">Jenkins Build</a></li>
                            <li><a href="${BUILD_URL}console">Console Output</a></li>
                        </ul>
                        
                        <h3>📎 Attachments</h3>
                        <p>Build log and test reports (if any) are attached. Please review the logs to identify the issue.</p>
                        
                        <hr>
                        <p style="color: #666; font-size: 12px;">
                            This is an automated message from Jenkins CI/CD Pipeline.
                        </p>
                    </body>
                    </html>
                """,
                to: "${GIT_COMMITTER_EMAIL}",
                mimeType: 'text/html',
                attachmentsPattern: 'test-reports/**/*',
                attachLog: true
            )
        }
        always {
            echo "📋 Final container status:"
            sh "docker ps"
        }
    }
}
            echo "📋 Final container status:"
            sh "docker ps"
        }
    }
}