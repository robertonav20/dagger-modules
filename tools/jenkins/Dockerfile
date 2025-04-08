FROM jenkins/jenkins:latest

ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false -Djenkins.install.SetupWizard.adminInitialApiToken=true"

# Install Jenkins plugins for Kubernetes, GITEA, Docker
RUN jenkins-plugin-cli --plugins \
    credentials \
    ansicolor:1.0.6 \
    workflow-aggregator:608.v67378e9d3db_1 \
    ws-cleanup:0.48

# Set Docker binaries in PATH
ENV PATH=$PATH:/usr/bin/docker

ARG GITEA_USER
ARG GITEA_PASSWORD
ARG NEXUS_USER
ARG NEXUS_PASSWORD
ARG JENKINS_ADMIN_PASSWORD
ARG JENKINS_ADMIN_USER
ARG JENKINS_FOLDER

ENV GITEA_USER=$GITEA_USER
ENV GITEA_PASSWORD=$GITEA_PASSWORD
ENV NEXUS_USER=$NEXUS_USER
ENV NEXUS_PASSWORD=$NEXUS_PASSWORD
ENV JENKINS_ADMIN_PASSWORD=$JENKINS_ADMIN_PASSWORD
ENV JENKINS_ADMIN_USER=$JENKINS_ADMIN_USER
ENV JENKINS_FOLDER=$JENKINS_FOLDER

# Switch back to Jenkins user
USER jenkins

# Copy Groovy scripts to initialize Jenkins with default credentials and folder
COPY groovy/ /usr/share/jenkins/ref/init.groovy.d/