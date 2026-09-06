package de.charlex.convention

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
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

internal fun VersionCatalog.version(name: String): String =
    findVersion(name)
        .orElseThrow { IllegalStateException("Version '$name' is missing from version catalog '${this.name}'") }
        .requiredVersion
