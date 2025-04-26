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

  /**
   * Publishes the application to a Docker registry.
   *
   * @param source     The directory containing the built artifacts (default: "/").
   * @param username   The username for authentication with the Docker registry (optional).
   * @param password   The password for authentication with the Docker registry (optional).
   * @param artifactRepository  The repository URL for the Docker image (optional).
   * @param artifactId        The ID of the Docker image (optional).
   * @param artifactVersion   The version of the Docker image (optional).
   * @param imageRepository  The repository URL for the Docker image.
   * @param imageName         The name of the Docker image.
   * @param imageVersion      The version of the Docker image.
   *
   * @return A string representing the result of the publishing operation.
   *
   * @throws InterruptedException if the thread is interrupted while waiting for the operation to complete.
   * @throws ExecutionException if an error occurs during execution of the operation.
   * @throws DaggerQueryException if a query-related error occurs.
   */
  @Function
  public String publish(
      @DefaultPath("/") Directory source,
      Optional<String> username,
      Optional<Secret> password,
      Optional<String> artifactRepository,
      Optional<String> artifactId,
      Optional<String> artifactVersion,
      String imageRepository,
      String imageName,
      String imageVersion)
      throws InterruptedException, ExecutionException, DaggerQueryException {
    test(source);
    Directory target = buildArtifact(source);

    if (password.isPresent()) {
      if (artifactRepository.isPresent() && artifactId.isPresent() && artifactVersion.isPresent()) {
        // deploy(target, username.get(), password.get(), artifactRepository.get(), artifactId.get(), artifactVersion.get());
      }
      return buildImage(target)
        .withRegistryAuth(imageRepository, username.get(), password.get())
        .publish(String.format("%s/%s:%s", imageRepository, imageName, imageVersion));
    }

    return buildImage(target)
      .publish(String.format("%s/%s:%s", imageRepository, imageName, imageVersion));
  }

  /**
   * Builds the application container. This method is responsible for compiling and packaging
   * the Java application into a container.
   *
   * @param source The directory containing the Java project.
   * @return A Directory object representing the built container.
   */
  @Function
  public Directory buildArtifact(@DefaultPath("/") Directory source)
      throws InterruptedException, ExecutionException, DaggerQueryException {
    return maven(source)
        .withExec(List.of("mvn", "clean", "package", "-DskipTests"))
        .directory("./target");
  }

  /**
   * Builds a Docker image from the provided build directory.
   *
   * @param build The directory containing the built application artifacts.
   * @return A Container object representing the built Docker image.
   */
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
    return maven(source).withExec(List.of("mvn", "test")).stdout();
  }

  /** Build a ready-to-use development environment */
  @Function
  public Container maven(@DefaultPath("/") Directory source)
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
      String username,
      Secret password,
      String url,
      String artifactId,
      String version)
      throws InterruptedException, ExecutionException, DaggerQueryException {
    // Assuming the built artifact is located in the target directory
    return maven(source)
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
