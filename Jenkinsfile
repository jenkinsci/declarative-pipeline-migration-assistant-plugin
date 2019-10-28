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
                                options: [junitPublisher(disabled: false)],
                                mavenLocalRepo: ".repository") {
                                sh "mvn -Djacoco.haltOnFailure=${params.failIfCoverageNotMet} -V -B clean install -e -Dmaven.test.failure.ignore=true"
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
                                sh "mvn -Djacoco.haltOnFailure=${params.failIfCoverageNotMet} -V -B clean install -e -Dmaven.test.failure.ignore=true"
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
                junit testResults: 'target/surefire-reports/*.xml', keepLongStdio: true
                archiveArtifacts "target/site/jacoco/jacoco.xml"
                jacoco (
                    classPattern: 'target/classes',
                    deltaBranchCoverage: '5',
                    deltaClassCoverage: '5',
                    deltaComplexityCoverage: '5',
                    deltaInstructionCoverage: '5',
                    deltaLineCoverage: '5',
                    deltaMethodCoverage: '5',
                    execPattern: 'target/jacoco.exec',
                    sourceInclusionPattern: '',
                    sourcePattern: 'src'
                )
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

