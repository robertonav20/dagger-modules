/**
 * A generated module for Workspace functions
 *
 * This module has been generated via dagger init and serves as a reference to
 * basic module structure as you get started with Dagger.
 *
 * Two functions have been pre-created. You can modify, delete, or add to them,
 * as needed. They demonstrate usage of arguments and return types using simple
 * echo and grep commands. The functions can be called from the dagger CLI or
 * from one of the SDKs.
 *
 * The first line in this comment block is a short description line and the
 * rest is a long description with more detail on the module's purpose or usage,
 * if appropriate. All modules should have a short description.
 */
import { dag, Container, Directory, Secret, object, func } from "@dagger.io/dagger"
import * as settings from "./settings.xml"

@object()
export class TypescriptBuild {

  /** Publish the application container after building and testing it on-the-fly */
  @func()
  async publish(source: Directory, imageRepository: string, imageName: string, imageVersion: string, artifactRepository?: string, artifactName?: string, artifactVersion?: string, username?: string, password?: Secret): Promise<string> {
    await this.test(source);
    const target = await this.buildArtifact(source);

    if (password) {
      if (artifactRepository && artifactName && artifactVersion) {
        await this.deploy(target, artifactRepository, artifactName, artifactVersion, username, password);
      }
      return this.buildImage(target)
        .withRegistryAuth(imageRepository, username, password)
        .publish(imageRepository + "/" + imageName + "-" + imageVersion);
    }

    return this.buildImage(target)
        .publish(imageRepository + "/" + imageName + "-" + imageVersion);
  }

  /** Build the application container */
  @func()
  buildImage(build: Directory): Container {
    return dag.container()
      .from("eclipse-temurin:17-jre-alpine")
      .withDirectory("/app", build)
      .withExposedPort(8080)
      .withEntrypoint(["java", "-jar", "/app/app.jar"]);
  }

  /** Return the result of running unit tests */
  @func()
  async test(source: Directory): Promise<string> {
    return this.maven()
        .withDirectory("/src", source)
        .withWorkdir("/src")
        .withExec(["mvn", "test"])
        .stdout();
  }

  /**
   * Builds the application container. This method is responsible for compiling and packaging
   * the Java application into a container.
   *
   * @param source The directory containing the Java project.
   * @return A Directory object representing the built container.
   */
  @func()
  buildArtifact(source: Directory): Container {
    return this.maven()
      .withDirectory("/src", source)
      .withWorkdir("/src")
      .withExec(["mvn", "clean", "package", "-DskipTests"])
      .directory("./target");
  }

  /** Deploy the application artifact to repository
   * @throws IOException */
  @func()
  deploy(target: Directory, url?: string, artifactId?: string, version?: string, username?: string, password?: Secret): String {
    return this.maven()
        .withEnvVariable("SERVER_USERNAME", username)
        .withEnvVariable("SERVER_PASSWORD", password.plaintext())
        .withDirectory("/target", target)
        .withWorkdir("/target")
        .withExec(["mvn", "deploy:deploy-file",
            "-DgeneratePom=true",
            "-DrepositoryId=server",
            "-Durl=" + url,
            "-Dfile=app.jar",
            "-DgroupId=com.example",
            "-DartifactId=" + artifactId,
            "-Dversion=" + version,
            "-Dpackaging=jar"])
        .stdout();
  }

  /** Build a ready-to-use development environment */
  @func()
  maven(): Container {
    return dag.container()
      .from("maven:3.9-eclipse-temurin-17-alpine")
      .withNewFile("/root/.m2/settings.xml", settings);
  }
}
