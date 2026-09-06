package de.charlex.convention

import org.gradle.api.Project
import java.util.Properties

internal fun Project.getLocalProperty(key: String): String? {
    val file = rootProject.file("local.properties")
    if (!file.exists()) return null
    return Properties().apply { file.inputStream().use { load(it) } }.getProperty(key)
}
