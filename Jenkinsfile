pipeline {
    agent any
    parameters {
        booleanParam defaultValue: true, description: 'Build fails if coverage falls below value set in project property jacoco.coverage.target<br><li>True (default): Enables this behavior<li>False: Disables this behavior', name: 'failIfCoverageNotMet'
    }
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
                                options: [junitPublisher(disabled: false),jacocoPublisher(disabled: false)]) {
                                sh "mvn -Penable-jacoco -Djacoco.haltOnFailure=${params.failIfCoverageNotMet} -V -B clean install -e -Dmaven.test.failure.ignore=true"
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
                                options: [junitPublisher(disabled: false)]) {
                                sh "mvn -Penable-jacoco -Djacoco.haltOnFailure=${params.failIfCoverageNotMet} -V -B clean install -e -Dmaven.test.failure.ignore=true"
                            }
                        }
                    }
                }
            }
        }
    }
        post {
            always {
                echo "--> We are finished with ${currentBuild.fullDisplayName}"
            }
            success {
                echo "--> SUCCESS: ${currentBuild.fullDisplayName}"
            }
            unstable {
                echo "--> UNSTABLE: ${currentBuild.fullDisplayName}"
            }
            failure {
                echo "--> FAILURE: ${currentBuild.fullDisplayName}"
            }
        }
}

