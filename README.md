<a href="https://repo1.maven.org/maven2/de/charlex/conventions/kmp/"><img src="https://img.shields.io/maven-central/v/de.charlex.conventions.kmp/catalog" alt="Maven Central" /></a>

# gradle-conventions-kotlin-multiplatform

Shared Gradle setup for the libraries published to Maven Central: build settings,
publishing and a version catalog with the build toolchain.

Two artifacts, always released at the same version:

| Artifact | Contents |
|---|---|
| `de.charlex.conventions.kmp:plugins` | the convention plugins |
| `de.charlex.conventions.kmp:catalog` | the shared toolchain (`gradle/libs.versions.toml`) |

Needs **Gradle 9.6 or newer**, because the catalog pins AGP 9.

## Plugins

| ID | |
|---|---|
| `de.charlex.convention.jvm.library` | plain JVM library |
| `de.charlex.convention.android.library` | Android library |
| `de.charlex.convention.kmp.library` | Kotlin Multiplatform for Android and iOS |
| `de.charlex.convention.publishing` | publishes a module to Maven Central |
| `de.charlex.convention.publishing.repository` | root project: the Sonatype staging repository |

`kmp.library` creates the Android and iOS targets itself -- a module needs no
`kotlin { }` block for its platforms.

Compose is not among them. Applying `org.jetbrains.compose` is two policy-free
lines, and most libraries do not use Compose. The Compose *compiler* plugin is in
the catalog, since it ships with Kotlin and follows the Kotlin version.

## Setup

`settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories { mavenCentral(); google() }

    versionCatalogs {
        create("conventions") {
            from("de.charlex.conventions.kmp:catalog:<version>")
        }
    }
}
```

The shared catalog is imported as `conventions`, not as `libs`. A repository keeps
its own `libs.versions.toml` for its own dependencies -- `libs` is what this
library uses, `conventions` is the shared toolchain. (A `version.ref` cannot cross
catalog boundaries, so they have to be separate.)

The convention plugins go into the repository's own `libs.versions.toml`:

```toml
[versions]
conventions = "<version>"   # same as the catalog coordinate above

[plugins]
convention-publishing-repository = { id = "de.charlex.convention.publishing.repository", version.ref = "conventions" }
convention-kmp-library = { id = "de.charlex.convention.kmp.library", version.ref = "conventions" }
convention-publishing = { id = "de.charlex.convention.publishing", version.ref = "conventions" }
```

Root `build.gradle.kts`:

```kotlin
plugins {
    // AGP and KGP: the conventions compile against them but leave the version
    // to you, so they have to be on the buildscript classpath.
    alias(conventions.plugins.android.library) apply false
    alias(conventions.plugins.kmp) apply false

    alias(libs.plugins.convention.publishing.repository)
    alias(libs.plugins.convention.kmp.library) apply false
    alias(libs.plugins.convention.publishing) apply false
}

subprojects {
    group = "de.charlex.something"
}
```

And a module:

```kotlin
plugins {
    alias(libs.plugins.convention.kmp.library)
    alias(libs.plugins.convention.publishing)
}

mavenPublishConfig {
    name = "my-library"
    description = "What it does."
}
```

## Publishing

`url` and the whole `scm` block default to the `origin` remote from `.git/config`,
the licence defaults to Apache-2.0. Usually only `name` and `description` are
needed. Everything else is optional:

```kotlin
mavenPublishConfig {
    name = "my-library"
    description = "What it does."
    artifactId = "published-under-another-name"

    developers {
        developer {
            id = "octocat"
            name = "The Octocat"
            email = "octocat@example.com"
        }
    }

    // licenseName, licenseUrl, url, scm { } if the defaults do not fit
}
```

Signing reads `SIGNING_KEY_ID`, `SIGNING_KEY` and `SIGNING_KEY_PASSWORD` from
`local.properties` first, then the environment. Without a key nothing is signed
instead of failing, so `publishToMavenLocal` works without credentials. The
staging repository uses `OSSRH_USERNAME`, `OSSRH_PASSWORD` and
`SONATYPE_STAGING_PROFILE_ID` the same way.


## Catalog entries the plugins read

`compileSdk`, `minSdk` and `jdk`. `jdk` drives the Gradle Java toolchain, from
which AGP and Kotlin derive their bytecode target -- it is the single place the
Java level is set.

For a migration period the plugins fall back to a catalog named `libs` when there
is no `conventions` catalog, so repositories can be switched over one at a time.

## Test projects

`test-projects/` holds one minimal library per shape -- JVM, Android and
multiplatform -- built with these conventions and published to mavenLocal:

```bash
cd test-projects
./gradlew :jvm-library:publishToMavenLocal
./gradlew :android-library:publishToMavenLocal
./gradlew :kmp-library:publishToMavenLocal
./verify-artifacts.sh
```

`verify-artifacts.sh` checks that every expected publication exists and carries
its main artifact, sources, javadoc and a POM with the required fields. CI runs
the same steps and the snapshot only goes out afterwards.

It is a separate Gradle build using `includeBuild("..")`, because a project cannot
apply a plugin produced by the same build -- which is also why this repository
writes its own publishing setup by hand in `build.gradle.kts`.
