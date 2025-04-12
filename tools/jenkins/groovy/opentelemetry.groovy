import io.jenkins.plugins.opentelemetry.JenkinsOpenTelemetryPluginConfiguration

def config = JenkinsOpenTelemetryPluginConfiguration.get()
config.setEndpoint("http://jaeger:4318")
config.setServiceName("jenkins")
config.setServiceNamespace("ci")
config.setConfigurationProperties("otel.exporter.otlp.protocol=http/protobuf\notel.traces.exporter=otlp\notel.metrics.exporter=none\notel.logs.exporter=none\ndeployment.environment=ci\n")
config.save()

println "OpenTelemetry configured successfully."
