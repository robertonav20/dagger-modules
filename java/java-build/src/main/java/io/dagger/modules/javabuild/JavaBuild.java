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

  /** Build the application container */
  @Function
  public Directory buildArtifact(@DefaultPath("/") Directory source)
      throws InterruptedException, ExecutionException, DaggerQueryException {
    return maven(source)
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
  // TODO configure credentials
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
