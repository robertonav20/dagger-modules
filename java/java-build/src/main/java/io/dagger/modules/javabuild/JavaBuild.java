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
  public String publish(@DefaultPath("/") Directory source, String repository, Optional<String> username, Optional<Secret> password, String appName, String version)
      throws InterruptedException, ExecutionException, DaggerQueryException {
    this.test(source);
    if (password.isPresent()) {
      return this.build(source)
        .withRegistryAuth(repository, username.get(), password.get())
        .publish(String.format("%s/%s:%s", repository, appName, version));
    }

    return this.build(source).publish(String.format("%s/%s:%s", repository, appName, version));
  }

  /** Build the application container */
  @Function
  public Container build(@DefaultPath("/") Directory source)
      throws InterruptedException, ExecutionException, DaggerQueryException {
    Directory build = this.buildEnv(source)
        .withExec(List.of("mvn", "clean", "package", "-DskipTests")).directory("./target");
    return dag().container().from("eclipse-temurin:17-jre-alpine").withDirectory("/app", build)
        .withExposedPort(8080).withEntrypoint(List.of("java", "-jar", "/app/app.jar"));
  }

  /** Return the result of running unit tests */
  @Function
  public String test(@DefaultPath("/") Directory source)
      throws InterruptedException, ExecutionException, DaggerQueryException {
    return this.buildEnv(source).withExec(List.of("mvn", "test")).stdout();
  }

  /** Build a ready-to-use development environment */
  @Function
  public Container buildEnv(@DefaultPath("/") Directory source)
      throws InterruptedException, ExecutionException, DaggerQueryException {
    CacheVolume m2Cache = dag().cacheVolume("m2");
    return dag().container().from("maven:3.9-eclipse-temurin-17-alpine")
        .withDirectory("/src", source).withMountedCache("/root/.m2", m2Cache).withWorkdir("/src");
  }
}
