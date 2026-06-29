pipeline {
  agent any
  tools {
    maven 'Maven3.9.16'
  }
  stages {
    stage('Build')  { steps { sh 'mvn -B clean compile' } }
    stage('Test')   { steps { sh 'mvn test' } }
    stage('Package'){ steps { sh 'mvn package -DskipTests' } }
  }
  post {
    always  { junit '**/target/surefire-reports/*.xml' }
    success { echo '✔ Pipeline green' }
    failure { echo '✗ Build failed' }
  }
}
