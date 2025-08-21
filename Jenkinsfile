pipeline {
    agent any

    environment {
        AWS_REGION = 'us-east-1'
        ECR_REGISTRY_URI = '730335490231.dkr.ecr.us-east-1.amazonaws.com/telegram-groq-bot'
        IMAGE_NAME = 'telegram-groq-bot'
        EC2_SERVER_IP = '35.175.190.100'
        AWS_CREDENTIALS_ID = 'aws-jenkins-credentials'
        EC2_SSH_KEY_ID = 'ec2-ssh-key'
        // ------------------------------------------
    }

    stages {
        stage('Checkout from GitHub') {
            steps {
                git branch: 'main', url: 'https://github.com/muhammadjonsaidov/intellij-groq-bot.git'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def dockerImage = docker.build("${IMAGE_NAME}:${env.BUILD_NUMBER}")
                }
            }
        }

        stage('Push Image to AWS ECR') {
            steps {
                script {
                    docker.withRegistry("https://${ECR_REGISTRY_URI}", "ecr:${AWS_REGION}:${AWS_CREDENTIALS_ID}") {
                        def imageUrl = "${ECR_REGISTRY_URI}/${IMAGE_NAME}:${env.BUILD_NUMBER}"
                        sh "docker tag ${IMAGE_NAME}:${env.BUILD_NUMBER} ${imageUrl}"
                        sh "docker push ${imageUrl}"

                        sh "docker tag ${imageUrl} ${ECR_REGISTRY_URI}/${IMAGE_NAME}:latest"
                        sh "docker push ${ECR_REGISTRY_URI}/${IMAGE_NAME}:latest"
                    }
                }
            }
        }

        stage('Deploy to EC2') {
            steps {
                sshagent([EC2_SSH_KEY_ID]) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@${EC2_SERVER_IP} '
                            cd intellij-groq-bot || exit 1

                            echo "ECR_IMAGE_URL=${ECR_REGISTRY_URI}/${IMAGE_NAME}:latest" > .env.prod

                            aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY_URI}

                            docker compose -f docker-compose.prod.yml pull

                            docker compose -f docker-compose.prod.yml up -d

                            docker image prune -f
                        '
                    """
                }
            }
        }
    }
}