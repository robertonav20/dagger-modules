package io.dagger.modules.javabuild;

import static io.dagger.client.Dagger.dag;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import io.dagger.client.CacheVolume;
import io.dagger.client.Container;
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
   * @throws Exception
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
      throws Exception {
    System.out.println("Running Test");
    test(source);

    System.out.println("Build Artifact");
    Directory target = buildArtifact(source);

    if (password.isPresent()) {
      if (artifactRepository.isPresent() && artifactId.isPresent() && artifactVersion.isPresent()) {
        System.out.println("Deploying artifact");
        deploy(target, username.get(), password.get(), artifactRepository.get(), artifactId.get(), artifactVersion.get());
      }

      System.out.println("Build and Push image");
      return buildImage(target)
        .withRegistryAuth(imageRepository, username.get(), password.get())
        .publish(String.format("%s/%s:%s", imageRepository, imageName, imageVersion));
    }

    System.out.println("Build and Push image");
    return buildImage(target)
      .publish(String.format("%s/%s:%s", imageRepository, imageName, imageVersion));
  }

  /**
   * Builds a Docker image from the provided build directory.
   *
   * @param build The directory containing the built application artifacts.
   * @return A Container object representing the built Docker image.
   */
  @Function
  public Container buildImage(@DefaultPath("/") Directory build)
      throws Exception {
    return dag().container()
        .from("eclipse-temurin:17-jre-alpine")
        .withDirectory("/app", build)
        .withExposedPort(8080)
        .withEntrypoint(List.of("java", "-jar", "/app/app.jar"));
  }

  /** Return the result of running unit tests */
  @Function
  public String test(@DefaultPath("/") Directory source)
      throws Exception {
    return maven()
        .withDirectory("/src", source)
        .withWorkdir("/src")
        .withExec(List.of("mvn", "test"))
        .stdout();
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
      throws Exception {
    return maven()
        .withDirectory("/src", source)
        .withWorkdir("/src")
        .withExec(List.of("mvn", "clean", "package", "-DskipTests"))
        .directory("./target");
  }

  /** Deploy the application artifact to repository
   * @throws IOException */
  @Function
  public String deploy(
      @DefaultPath("/") Directory target,
      String username,
      Secret password,
      String url,
      String artifactId,
      String version)
      throws Exception {
    return maven()
        .withEnvVariable("SERVER_USERNAME", username)
        .withEnvVariable("SERVER_PASSWORD", password.plaintext())
        .withDirectory("/target", target)
        .withWorkdir("/target")
        .withExec(List.of("mvn", "deploy:deploy-file",
            "-DgeneratePom=true",
            "-DrepositoryId=server",
            "-Durl=" + url,
            "-Dfile=app.jar",
            "-DgroupId=com.example",
            "-DartifactId=" + artifactId,
            "-Dversion=" + version,
            "-Dpackaging=jar"))
        .stdout();
  }

  /** Build a ready-to-use development environment */
  @Function
  public Container maven()
      throws Exception {
    String settings = new String(Thread.currentThread().getContextClassLoader().getResourceAsStream("settings.xml").readAllBytes(), StandardCharsets.UTF_8);
    CacheVolume m2RepositoryCache = dag().cacheVolume("m2Repository");
    return dag().container()
        .from("maven:3.9-eclipse-temurin-17-alpine")
        .withMountedCache("/root/.m2/repository", m2RepositoryCache)
        .withNewFile("/root/.m2/settings.xml", settings);
  }
}
