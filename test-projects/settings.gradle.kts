// Separate Gradle build on purpose: a project cannot apply a plugin that is built
// by the same build, so the convention plugins are consumed from the parent build
// via includeBuild.
pluginManagement {
    includeBuild("..")

    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
        google()
    }

    versionCatalogs {
        create("conventions") {
            // The same file that is published as de.charlex.conventions.kmp:catalog.
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "test-projects"

include(":jvm-library")
include(":android-library")
include(":kmp-library")
