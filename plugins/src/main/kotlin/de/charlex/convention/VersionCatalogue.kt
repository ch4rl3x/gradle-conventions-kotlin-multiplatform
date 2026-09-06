package de.charlex.convention

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 * The shared toolchain catalog, published as de.charlex.conventions.kmp:catalog.
 *
 * Looks for a catalog named "conventions" and falls back to "libs", so a project
 * that has not split its catalog yet keeps working. That makes it possible to
 * migrate repositories one at a time.
 *
 * Deliberately untyped instead of the generated `LibrariesForLibs` accessors:
 * that class is not part of the plugin jar, it comes from a local Gradle cache
 * via a `files(...)` dependency -- and those are dropped when a plugin is
 * published to a repository.
 */
val Project.conventionCatalog: VersionCatalog
    get() {
        val catalogs = extensions.getByType<VersionCatalogsExtension>()
        return catalogs.find("conventions")
            .or { catalogs.find("libs") }
            .orElseThrow {
                IllegalStateException(
                    "No version catalog named 'conventions' or 'libs' found. Import the shared " +
                        "catalog in settings.gradle.kts:\n" +
                        "  dependencyResolutionManagement {\n" +
                        "      versionCatalogs {\n" +
                        "          create(\"conventions\") { from(\"de.charlex.conventions.kmp:catalog:<version>\") }\n" +
                        "      }\n" +
                        "  }"
                )
            }
    }

/**
 * Required version for [name], with an error message that names the missing entry
 * instead of failing with a bare NoSuchElementException.
 */
fun VersionCatalog.version(name: String): String =
    findVersion(name)
        .orElseThrow { IllegalStateException("Version '$name' is missing from version catalog '${this.name}'") }
        .requiredVersion
