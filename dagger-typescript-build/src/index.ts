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
import { dag, Container, Directory, object, func } from "@dagger.io/dagger"

@object()
export class TypescriptBuild {

  /** Publish the application container after building and testing it on-the-fly */
  @func()
  async publish(source: Directory, appName: string, version: string): Promise<string> {
    await this.test(source);
    return this.build(source).publish("ttl.sh/" + appName + "." + version);
  }

  /** Build the application container */
  @func()
  build(source: Directory): Container {
    const build = this.buildEnv(source)
      .withExec(["mvn", "clean", "package", "-DskipTests"])
      .directory("./target");
    return dag.container()
      .from("eclipse-temurin:17-jre-alpine")
      .withDirectory("/app", build)
      .withExposedPort(8080)
      .withEntrypoint(["java", "-jar", "/app/app.jar"]);
  }

  /** Return the result of running unit tests */
  @func()
  async test(source: Directory): Promise<string> {
    return this.buildEnv(source).withExec(["mvn", "test"]).stdout();
  }

  /** Build a ready-to-use development environment */
  @func()
  buildEnv(source: Directory): Container {
    const m2Cache = dag.cacheVolume("m2");
    return dag.container()
      .from("maven:3.9-eclipse-temurin-17-alpine")
      .withDirectory("/src", source)
      .withMountedCache("/root/.m2", m2Cache)
      .withWorkdir("/src");
  }
}
