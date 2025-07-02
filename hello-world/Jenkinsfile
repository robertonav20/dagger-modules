pipeline {
    agent any

    options {
        ansiColor('xterm')
    }

    environment {
        DAGGER_NO_NAG = 1
        DAGGER_VERSION = "0.18.3"
        PATH = "/tmp/dagger/bin:$PATH"
        _EXPERIMENTAL_DAGGER_RUNNER_HOST = "tcp://dagger:1234"
    }

    stages {
        stage("dagger") {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-credentials', passwordVariable: 'NEXUS_PASSWORD', usernameVariable: 'NEXUS_USER')]) {
                    sh '''
                        curl -fsSL https://dl.dagger.io/dagger/install.sh | BIN_DIR=/tmp/dagger/bin DAGGER_VERSION=$DAGGER_VERSION sh
                        rm -rf java-dagger-module && git clone http://gitea:3000/admin/java-dagger-module/ java-dagger-module
                        dagger call -m ./java-dagger-module publish \
                        --source=./ \
                        --username $NEXUS_USER \
                        --password env:NEXUS_PASSWORD \
                        --artifact-repository "http://nexus.local/repository/maven-releases" \
                        --artifact-id "hello-world" \
                        --artifact-version 1.0.0 \
                        --image-repository "nexus.local" \
                        --image-name "docker-repo/hello-world" \
                        --image-version 1.0.0
                    '''
                }
            }
        }
    }
}