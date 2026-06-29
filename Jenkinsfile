pipeline {
  agent any
  stages {
    stage('Build')  { steps { sh 'echo "build the project..."' } }
    stage('Test')   { steps { sh 'echo "test the project..."' } }
    stage('Package'){ steps { sh 'echo "package the project..."' } }
  }
  post {
    always  { junit '**/target/surefire-reports/*.xml' }
    success { echo '✔ Pipeline green' }
    failure { echo '✗ Build failed' }
  }
}