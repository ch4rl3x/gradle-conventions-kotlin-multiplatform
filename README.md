# gradle-conventions-kotlin-multiplatform

Shared Gradle convention plugins and version catalog for Kotlin Multiplatform
libraries published to Maven Central.

Nothing here is tied to a particular group or GitHub account: the Android
namespace comes from a Gradle property, and the POM's `url` and `scm` are derived
from the repository's own git remote.

Published as two artifacts at the same version:

| Artifact | Contents |
|---|---|
| `de.charlex.conventions.kmp:plugins` | the six convention plugins below |
| `de.charlex.conventions.kmp:catalog` | the shared build toolchain (`gradle/libs.versions.toml`) |

They are versioned together on purpose. The plugins compile against the AGP,
Kotlin and Dokka APIs via `compileOnly`, so a catalog that pins different
versions than the plugins were built against fails at configuration time.

## Plugins

| ID | |
|---|---|
| `de.charlex.convention.root` | root project: applies and configures `nexusPublishing` for the Sonatype staging repository |
| `de.charlex.convention.android.library` | `compileSdk`/`minSdk`, optional namespace convention, consumer ProGuard rules |
| `de.charlex.convention.android.application` | same for application modules, plus `targetSdk` |
| `de.charlex.convention.kotlin.multiplatform` | KMP plus the mobile convention below |
| `de.charlex.convention.kotlin.multiplatform.mobile` | Android and iOS targets, default hierarchy template, JVM toolchain |
| `de.charlex.convention.centralPublish` | javadoc jar, POM, signing, Maven Central publication |

The mobile plugin declares `iosX64`, `iosArm64` and `iosSimulatorArm64` itself, so
a library module needs no `kotlin { }` block for its platforms. It skips the iOS
targets when `com.android.application` is applied, since an Android app module has
no iOS counterpart. Configuring a framework binary is left to whichever module is
actually linked from Xcode — that module calls `binaries.framework { }` itself,
where it can also set `baseName` and exports.

Compose is deliberately not among them: applying `org.jetbrains.compose` is a
two-line, policy-free step, and most KMP libraries do not use Compose at all. The
Compose *compiler* plugin does live in the shared catalog, because it ships with
Kotlin and is always on the Kotlin version.

## Usage

`settings.gradle.kts` of the consuming repository:

```kotlin
pluginManagement {
    repositories { mavenCentral(); google(); gradlePluginPortal() }
    plugins {
        id("de.charlex.convention.root") version "<version>"
        id("de.charlex.convention.android.library") version "<version>"
        // ... the plugins this repository applies
    }
}

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

The shared catalog is imported as `conventions`, not as `libs`: a repository keeps
its own `gradle/libs.versions.toml` for its own dependencies, and a `version.ref`
cannot cross catalog boundaries. `libs` is what this library uses, `conventions`
is the shared toolchain.

For a migration period the plugins fall back to a catalog named `libs` when no
`conventions` catalog exists, so repositories can be switched over one at a time.

The catalog has to provide `compileSdk`, `targetSdk`, `minSdk` and `jvmTarget`;
a missing entry fails with a message naming it.

### Android namespace

`de.charlex.convention.android.library` derives the namespace from the Gradle
property `convention.android.namespacePrefix` and the module name — prefix
`com.example` and module `my-library` give `com.example.my.library`:

```properties
convention.android.namespacePrefix=com.example
```

Without the property the plugin leaves `namespace` alone, and the module sets it.

### Publishing

`de.charlex.convention.centralPublish` adds a `mavenPublishConfig` extension. Only
`name` and `description` are usually needed — `url` and the whole `scm` block
default to the `origin` remote read from `.git/config`, and the licence defaults to
Apache-2.0:

```kotlin
mavenPublishConfig {
    name = "my-library"
    description = "What it does."

    developers {
        developer {
            id = "octocat"
            name = "The Octocat"
            email = "octocat@example.com"
        }
    }

    // optional: licenseName, licenseUrl, url, scm { }
}
```

Signing uses `SIGNING_KEY_ID`, `SIGNING_KEY` and `SIGNING_KEY_PASSWORD`, taken from
`local.properties` first and then the environment. Without a key signing is
skipped rather than failing, so `publishToMavenLocal` works without credentials.

## This repository cannot use its own plugins

Gradle cannot `includeBuild` the build it is currently running, so the publishing
setup in `build.gradle.kts` is written out by hand -- including
`de.charlex.convention.centralPublish`, which it cannot apply to itself either.

CI uses the `jvm-snapshot.yml` workflow from
[maven-central-publish-pipeline](https://github.com/ch4rl3x/maven-central-publish-pipeline),
which runs on `ubuntu-latest` — there are no Apple targets here.
