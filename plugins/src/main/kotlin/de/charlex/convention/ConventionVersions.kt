package de.charlex.convention

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog

/**
 * Typed view of the version entries these convention plugins rely on.
 *
 * This replaces Gradle's generated `LibrariesForLibs` accessors. Those cannot
 * survive publication: the generated class is not part of the plugin jar, it sits
 * in a local Gradle cache jar that a `files(...)` dependency put on the plugin
 * classpath -- and `files(...)` dependencies are dropped when a plugin is
 * published to a repository.
 *
 * Adding an entry here is the single place that has to change when the plugins
 * start depending on another version from the catalog.
 */
class ConventionVersions(private val catalog: VersionCatalog) {
    val jvmTarget: String get() = catalog.version("jvmTarget")
    val compileSdk: Int get() = catalog.version("compileSdk").toInt()
    val minSdk: Int get() = catalog.version("minSdk").toInt()
    val targetSdk: Int get() = catalog.version("targetSdk").toInt()
}

val Project.conventionVersions: ConventionVersions
    get() = ConventionVersions(conventionCatalog)
