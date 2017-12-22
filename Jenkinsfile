pipeline {
  agent none
  stages {
    stage('Build') {
      steps {
        sh 'mvn clean install'
      }
    }
    stage('Archive Artifacs') {
      steps {
        archiveArtifacts 'target/**.jar'
      }
    }
  }
}