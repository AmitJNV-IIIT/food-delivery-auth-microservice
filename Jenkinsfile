pipeline {
    agent any
    environment {
        // maven path
        PATH = "/usr/bin/mvn:$PATH"
    }

    stages {
        stage('Clean Up') {
            steps {
                script {

                    sh "dir"
                    sh "rm -rf case-study-food-delivery-amit-kumar"
                    echo "Directory 'case-study-food-delivery-amit-kumar deleted."
                }
            }
        }

        stage('Clone sources') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'token', passwordVariable: 'BIT_BUCKET_PASSWORD', usernameVariable: 'BIT_BUCKET_USERNAME')]) {
                    sh 'git clone https://${BIT_BUCKET_USERNAME}:${BIT_BUCKET_PASSWORD}@tools.publicis.sapient.com/bitbucket/scm/psir/case-study-food-delivery-amit-kumar.git'
                }
            }
        }
        stage('Checkout branch') {
            steps {
                script {

                    dir('Case-Study Food Delivery Amit-Kumar/auth_microservice') {

                        sh "git checkout master"
                    }
                }
            }
        }
        stage('Build') {
            steps {
                dir("case-study-food-delivery-amit-kumar/auth_microservice") {
                    sh "mvn clean install -DskipTests"
                }
            }
        }
         stage('Test') {
                    steps {
                        dir("case-study-food-delivery-amit-kumar/auth_microservice") {
                            sh "mvn test"
                        }
                    }
                }
        stage('Image Build') {
            environment {
                PATH = "/usr/bin/docker:$PATH"
            }
            steps {
                dir("case-study-food-delivery-amit-kumar/auth_microservice") {
                    sh "docker build -t auth_microservice ."
                }
            }
        }
        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-cred', passwordVariable: 'dockerHubPassword', usernameVariable: 'dockerHubUser')]) {
                    sh "docker login -u ${env.dockerHubUser} -p ${env.dockerHubPassword}"
                    sh "docker tag auth_microservice amikumar114/auth_microservice"
                    sh "docker push amikumar114/auth_microservice"
                }
            }
        }
    }
}