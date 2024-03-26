#!/usr/bin/env groovy

pipeline {
  agent any
    stages {
      stage('Build') {
        stage("Initialization") {
          steps {
            script {
              def version = sh(returnStdout: true, script: 'docker compose run --rm maven mvn $MVN_OPTS help:evaluate -Dexpression=project.version -q -DforceStdout')
              buildName "${env.GIT_BRANCH.replace("origin/", "")}@${version}"
            }
          }
        }
        steps {
          checkout scm
          sh './build.sh init clean install publish'
        }
      }
    }
}

