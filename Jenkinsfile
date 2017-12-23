pipeline {
  agent any
  stages {
    stage('Initialize') {
      steps {
        sh 'mvn clean'
      }
    }
    stage('Build') {
      steps {
        sh 'mvn install'
      }
    }
    stage('Archive artifacts') {
      steps {
        archiveArtifacts 'target/**.jar'
      }
    }
  }
}