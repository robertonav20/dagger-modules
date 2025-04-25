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

@object()
export class TypescriptBuild {

  /** Publish the application container after building and testing it on-the-fly */
  @func()
  async publish(source: Directory, imageRepository: string, imageName: string, imageVersion: string, artifactRepository?: string, artifactName?: string, artifactVersion?: string, username?: string, password?: Secret): Promise<string> {
    await this.test(source);
    const target = await this.buildArtifact(source);

    if (password) {
      if (artifactRepository && artifactName && artifactVersion) {
        // await this.deploy(target, artifactRepository, artifactName, artifactVersion, username, password);
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
  buildArtifact(source: Directory): Container {
    return this.maven(source)
      .withExec(["mvn", "clean", "package", "-DskipTests"])
      .directory("./target");
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
    return this.maven(source).withExec(["mvn", "test"]).stdout();
  }

  /** Build a ready-to-use development environment */
  @func()
  maven(source: Directory): Container {
    const m2Cache = dag.cacheVolume("m2");
    return dag.container()
      .from("maven:3.9-eclipse-temurin-17-alpine")
      .withDirectory("/src", source)
      .withMountedCache("/root/.m2", m2Cache)
      .withWorkdir("/src");
  }

  /** Build a ready-to-use development environment */
  // TODO configure credentials
  @func()
  deploy(source: Directory, url?: string, artifactId?: string, version?: string, username?: string, password?: Secret): Container {
    const m2Cache = dag.cacheVolume("m2");
    return this.maven(source)
        .withEnvVariable("NEXUS_SERVER_USERNAME", username)
        .withEnvVariable("NEXUS_SERVER_PASSWORD", password.plaintext())
        .withExec(["mvn", "deploy:deploy-file",
            "-DgroupId=com.example",
            "-DartifactId=" + artifactId,
            "-Dversion=" + version,
            "-Dpackaging=jar",
            "-Dfile=app.jar",
            "-DrepositoryId=maven-releases",
            "-Durl=" + url,
            "-DserverId=nexus-server"]);
  }
}
