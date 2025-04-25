package io.dagger.modules.javabuild;

import static io.dagger.client.Dagger.dag;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import io.dagger.client.CacheVolume;
import io.dagger.client.Container;
import io.dagger.client.DaggerQueryException;
import io.dagger.client.Directory;
import io.dagger.client.Secret;
import io.dagger.module.annotation.DefaultPath;
import io.dagger.module.annotation.Function;
import io.dagger.module.annotation.Object;

/** SpringPetclinic main object */
@Object
public class JavaBuild {

  /** Publish the application container after building and testing it on-the-fly */
  @Function
  public String publish(
      @DefaultPath("/") Directory source,
      String repository,
      Optional<String> username,
      Optional<Secret> password,
      String appName,
      String version)
      throws InterruptedException, ExecutionException, DaggerQueryException {
    test(source);
    Directory target = buildArtifact(source);

    if (password.isPresent()) {
      deploy(target, repository, username.get(), password.get(), appName, version);
      return buildImage(target)
        .withRegistryAuth(repository, username.get(), password.get())
        .publish(String.format("%s/%s:%s", repository, appName, version));
    }

    return buildImage(target)
      .publish(String.format("%s/%s:%s", repository, appName, version));
  }

  /** Build the application container */
  @Function
  public Directory buildArtifact(@DefaultPath("/") Directory source)
      throws InterruptedException, ExecutionException, DaggerQueryException {
    return buildEnv(source)
        .withExec(List.of("mvn", "clean", "package", "-DskipTests"))
        .directory("./target");
  }

  /** Build the application container */
  @Function
  public Container buildImage(@DefaultPath("/") Directory build)
      throws InterruptedException, ExecutionException, DaggerQueryException {
    return dag().container()
        .from("eclipse-temurin:17-jre-alpine")
        .withDirectory("/app", build)
        .withExposedPort(8080)
        .withEntrypoint(List.of("java", "-jar", "/app/app.jar"));
  }

  /** Return the result of running unit tests */
  @Function
  public String test(@DefaultPath("/") Directory source)
      throws InterruptedException, ExecutionException, DaggerQueryException {
    return buildEnv(source).withExec(List.of("mvn", "test")).stdout();
  }

  /** Build a ready-to-use development environment */
  @Function
  public Container buildEnv(@DefaultPath("/") Directory source)
      throws InterruptedException, ExecutionException, DaggerQueryException {
    CacheVolume m2Cache = dag().cacheVolume("m2");
    return dag().container()
        .from("maven:3.9-eclipse-temurin-17-alpine")
        .withDirectory("/src", source)
        .withMountedCache("/root/.m2", m2Cache)
        .withWorkdir("/src");
  }

  /** Deploy the application artifact to Nexus repository */
  @Function
  public Container deploy(
      @DefaultPath("/") Directory source,
      String url,
      String username,
      Secret password,
      String artifactId,
      String version)
      throws InterruptedException, ExecutionException, DaggerQueryException {
    // Assuming the built artifact is located in the target directory
    return buildEnv(source)
        .withEnvVariable("NEXUS_SERVER_USERNAME", username)
        .withEnvVariable("NEXUS_SERVER_PASSWORD", password.plaintext())
        .withExec(List.of("mvn", "deploy:deploy-file",
            "-DgroupId=com.example",
            "-DartifactId=" + artifactId,
            "-Dversion=" + version,
            "-Dpackaging=jar",
            "-Dfile=app.jar",
            "-DrepositoryId=maven-releases",
            "-Durl=" + url,
            "-DserverId=nexus-server"));
  }
}
