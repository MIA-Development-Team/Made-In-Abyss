#!/usr/bin/env groovy

pipeline {
    agent any

    tools {
        jdk "jdk-21"
    }

    stages {
        stage('Setup') {
            steps {
                echo 'Setup Project'
                sh 'chmod +x gradlew'
                sh './gradlew clean'
            }
        }

        stage('Build') {
            steps {
                withCredentials([
                    usernamePassword(credentialsId: 'JENKINS_MAVEN', usernameVariable: 'MAVEN_USERNAME', passwordVariable: 'MAVEN_PASSWORD')
                ]) {
                    echo 'Building project'
                    sh './gradlew build publish'
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true
        }
    }
}