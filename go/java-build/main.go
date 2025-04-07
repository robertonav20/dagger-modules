// A generated module for JavaBuild functions
//
// This module has been generated via dagger init and serves as a reference to
// basic module structure as you get started with Dagger.
//
// Two functions have been pre-created. You can modify, delete, or add to them,
// as needed. They demonstrate usage of arguments and return types using simple
// echo and grep commands. The functions can be called from the dagger CLI or
// from one of the SDKs.
//
// The first line in this comment block is a short description line and the
// rest is a long description with more detail on the module's purpose or usage,
// if appropriate. All modules should have a short description.

package main

import (
	"context"
	"dagger/java-build/internal/dagger"
)

type JavaBuild struct{}

func (m *JavaBuild) Publish(source *dagger.Directory, repository string, appName string, version string) (string, error) {
	Test(source)
	ctx := context.Background()
	return Build(source).
		Publish(ctx, repository + "/" + appName + ":" + version)
}

/** Build the application container */
func Build(source *dagger.Directory) *dagger.Container {
	var build = BuildEnv(source).
		WithExec([]string{"mvn", "clean", "package", "-DskipTests"}).
		Directory("./target")
	return dag.
		Container().
		From("eclipse-temurin:17-jre-alpine").
		WithDirectory("/app", build).
		WithExposedPort(8080).
		WithEntrypoint([]string{"java", "-jar", "/app/app.jar"})
}

func Test(source *dagger.Directory) (string, error) {
	ctx := context.Background()
	return BuildEnv(source).
		WithExec([]string{"mvn", "test"}).
		Stdout(ctx)
}

// Returns lines that match a pattern in the files of the provided Directory
func BuildEnv(source *dagger.Directory) *dagger.Container {
	var m2Cache = dag.CacheVolume("m2")
	return dag.
		Container().
		From("maven:3.9-eclipse-temurin-17-alpine").
		WithDirectory("/src", source).
		WithMountedCache("/root/.m2", m2Cache).
		WithWorkdir("/src")
}
