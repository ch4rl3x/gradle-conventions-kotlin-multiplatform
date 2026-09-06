package de.charlex.convention

import org.gradle.api.Project
import java.util.Properties

/**
 * Reads [key] from local.properties in the root of the build, or null if the file
 * or the key is absent.
 *
 * Replaces org.jetbrains.compose.internal.utils.getLocalProperty: that is internal
 * API of the Compose Gradle plugin and tied publishing to Compose being on the
 * classpath.
 */
fun Project.getLocalProperty(key: String): String? {
    val file = rootProject.file("local.properties")
    if (!file.exists()) return null
    return Properties().apply { file.inputStream().use { load(it) } }.getProperty(key)
}
