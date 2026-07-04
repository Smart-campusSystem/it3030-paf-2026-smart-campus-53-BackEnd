// =============================================================================
// Smart Campus Backend — Jenkins Pipeline
// =============================================================================
// Prerequisites:
//   1. Jenkins plugins: Pipeline, Git, SSH Agent, Maven
//   2. Jenkins credentials:
//      - 'ec2-ssh-key'     : SSH private key for EC2
//      - 'aws-credentials' : AWS Access Key + Secret Key
//   3. Jenkins global tools:
//      - JDK 21 named 'JDK-21'
//   4. Environment variable EC2_HOST set in Jenkins config
// =============================================================================

pipeline {
    agent any

    tools {
        jdk 'JDK-21'
    }

    environment {
        EC2_HOST       = credentials('ec2-host')  // or set as pipeline param
        APP_DIR        = '/opt/smart-campus'
        HEALTH_URL     = "http://${EC2_HOST}:8080/api/health"
    }

    options {
        timeout(time: 15, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    triggers {
        // GitHub webhook via ngrok — Jenkins receives POST on push
        githubPush()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo "Branch: ${env.GIT_BRANCH}"
                echo "Commit: ${env.GIT_COMMIT}"
            }
        }

        stage('Test') {
            steps {
                echo '🧪 Running tests...'
                bat 'mvnw.cmd test'  // Use 'sh ./mvnw test' on Linux Jenkins
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Build') {
            steps {
                echo '🔨 Building JAR...'
                bat 'mvnw.cmd package -DskipTests -B'  // Use 'sh ./mvnw ...' on Linux
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Deploy to EC2') {
            steps {
                echo '🚀 Deploying to EC2...'
                sshagent(credentials: ['ec2-ssh-key']) {
                    // Upload JAR
                    sh """
                        scp -o StrictHostKeyChecking=no target/*.jar ec2-user@${EC2_HOST}:/tmp/app.jar
                    """

                    // Stop, deploy, start
                    sh """
                        ssh -o StrictHostKeyChecking=no ec2-user@${EC2_HOST} '
                            sudo systemctl stop smart-campus || true
                            sleep 2
                            [ -f ${APP_DIR}/app.jar ] && sudo cp ${APP_DIR}/app.jar ${APP_DIR}/app.jar.backup
                            sudo mv /tmp/app.jar ${APP_DIR}/app.jar
                            sudo chown smartcampus:smartcampus ${APP_DIR}/app.jar
                            sudo systemctl start smart-campus
                        '
                    """
                }
            }
        }

        stage('Health Check') {
            steps {
                echo '🏥 Checking application health...'
                sshagent(credentials: ['ec2-ssh-key']) {
                    sh """
                        RETRIES=0
                        MAX_RETRIES=15
                        until [ \$RETRIES -ge \$MAX_RETRIES ]; do
                            RETRIES=\$((RETRIES + 1))
                            echo "  Attempt \$RETRIES/\$MAX_RETRIES..."
                            STATUS=\$(ssh -o StrictHostKeyChecking=no ec2-user@${EC2_HOST} \\
                                'curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/health' 2>/dev/null || echo "000")
                            if [ "\$STATUS" = "200" ]; then
                                echo "✅ Application is healthy!"
                                ssh -o StrictHostKeyChecking=no ec2-user@${EC2_HOST} 'curl -s http://localhost:8080/api/health'
                                exit 0
                            fi
                            sleep 5
                        done
                        echo "❌ Health check failed!"
                        exit 1
                    """
                }
            }
        }
    }

    post {
        failure {
            echo '❌ Build failed! Attempting rollback...'
            sshagent(credentials: ['ec2-ssh-key']) {
                sh """
                    ssh -o StrictHostKeyChecking=no ec2-user@${EC2_HOST} '
                        sudo systemctl stop smart-campus || true
                        if [ -f ${APP_DIR}/app.jar.backup ]; then
                            sudo mv ${APP_DIR}/app.jar.backup ${APP_DIR}/app.jar
                            sudo systemctl start smart-campus
                            echo "Rolled back to previous version."
                        fi
                    ' || true
                """
            }
        }
        success {
            echo '✅ Backend deployed successfully!'
        }
        always {
            cleanWs()
        }
    }
}
