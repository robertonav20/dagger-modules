FROM jenkins/jenkins:2.506-jdk17

ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false -Djenkins.install.SetupWizard.adminInitialApiToken=true"

# Install Jenkins plugins for Kubernetes, GITEA, Docker
RUN jenkins-plugin-cli --plugins \
    credentials \
    ansicolor:1.0.6 \
    workflow-aggregator:608.v67378e9d3db_1 \
    ws-cleanup:0.48 \
    gitea:238.v626a_cf5e1940 \
    opentelemetry:3.1523.v0b_3ce640987d \
    pipeline-graph-view:504.vb_643f0e2e9c0

# Set Docker binaries in PATH
# ENV PATH=$PATH:/usr/bin/docker

ARG GITEA_USER
ARG GITEA_PASSWORD
ARG NEXUS_USER
ARG NEXUS_PASSWORD
ARG JENKINS_PASSWORD
ARG JENKINS_USER

ENV GITEA_USER=$GITEA_USER
ENV GITEA_PASSWORD=$GITEA_PASSWORD
ENV NEXUS_USER=$NEXUS_USER
ENV NEXUS_PASSWORD=$NEXUS_PASSWORD
ENV JENKINS_PASSWORD=$JENKINS_PASSWORD
ENV JENKINS_USER=$JENKINS_USER

# Switch back to Jenkins user
USER jenkins

# Copy Groovy scripts to initialize Jenkins with default credentials and folder
ADD groovy/admin.groovy /usr/share/jenkins/ref/init.groovy.d/
ADD groovy/gitea.groovy /usr/share/jenkins/ref/init.groovy.d/
ADD groovy/nexus.groovy /usr/share/jenkins/ref/init.groovy.d/
ADD groovy/opentelemetry.groovy /usr/share/jenkins/ref/init.groovy.d/