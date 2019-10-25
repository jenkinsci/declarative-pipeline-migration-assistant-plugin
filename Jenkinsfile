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
        post {
            always {
                echo "--> ALWAYS: We are finished with ${currentBuild.fullDisplayName}"
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
                slackSend(
                    message: 'Successful build: currentBuild.fullDisplayName'
                    color: ‘good’,
                    channel: ‘#team-pipeline’
                )
            }
            unstable {
                echo "--> UNSTABLE: ${currentBuild.fullDisplayName}"
                slackSend(
                    message: 'Ustable build: currentBuild.fullDisplayName'
                    color: ‘good’,
                    channel: ‘#team-pipeline’
                )
            }
            failure {
                echo "--> FAILURE: ${currentBuild.fullDisplayName}"
                slackSend(
                    message: 'Failed build: currentBuild.fullDisplayName'
                    color: ‘bad’,
                    channel: ‘#team-pipeline’
                )
            }
        }
}

