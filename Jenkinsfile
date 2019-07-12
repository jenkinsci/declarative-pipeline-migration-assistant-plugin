pipeline {
  agent any
  stages {
    stage("Parallel Stage") {
        parallel {
            stage("linux build"){
                agent { node { label 'linux' } }
                steps {
                        
                    timeout( time: 180, unit: 'MINUTES' ) {
                        withMaven(
                              maven: 'maven3.6.1',
                              publisherStrategy: 'EXPLICIT',
                              options: [junitPublisher(disabled: false)],
                              mavenLocalRepo: ".repository") {
                            sh "mvn -V -B clean install -e -Dmaven.test.failure.ignore=true"
                        }  
                    }
                }    
            }
            stage("windows build"){
                agent { node { label 'windows' } }
                steps {
                        
                    timeout( time: 180, unit: 'MINUTES' ) {
                        withMaven(
                              maven: 'maven3.6.1',
                              publisherStrategy: 'EXPLICIT',
                              options: [junitPublisher(disabled: false)],
                              mavenLocalRepo: ".repository") {
                            sh "mvn -V -B clean install -e -Dmaven.test.failure.ignore=true"
                        }  
                    }
                }    
            }        
        }
    }    
  }    
}  
  
