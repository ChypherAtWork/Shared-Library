pipeline {
    agent any
    
    stages {
        stage('Build') {
            steps {
                script {
                    def groovyScript = load 'example.groovy'
                    groovyScript.call()
                }
            }
        }
    }
}
