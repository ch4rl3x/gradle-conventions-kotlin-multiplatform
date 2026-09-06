package de.charlex.convention

import org.gradle.api.Project
import java.io.File

/**
 * URL of the `origin` remote, normalised to a browsable https URL such as
 * `https://github.com/owner/repo`, or null when there is no git checkout or no
 * origin remote.
 *
 * Read from .git/config instead of shelling out to git, so it needs no git on
 * PATH and spawns no process during configuration.
 */
internal fun Project.originUrl(): String? {
    val gitDir = File(rootDir, ".git")
    if (!gitDir.isDirectory) return null // plain checkouts only; worktrees store a file here

    val config = File(gitDir, "config")
    if (!config.isFile) return null

    var inOrigin = false
    for (raw in config.readLines()) {
        val line = raw.trim()
        if (line.startsWith("[")) {
            inOrigin = line.replace(" ", "") == "[remote\"origin\"]"
            continue
        }
        if (inOrigin && line.startsWith("url")) {
            return line.substringAfter("=").trim().takeIf { it.isNotEmpty() }?.let(::normaliseRemote)
        }
    }
    return null
}

/**
 * `git@host:owner/repo.git`, `ssh://git@host/owner/repo.git` and
 * `https://host/owner/repo.git` all become `https://host/owner/repo`.
 */
private fun normaliseRemote(remote: String): String {
    var url = remote.removeSuffix(".git")
    url = when {
        url.startsWith("git@") -> "https://" + url.removePrefix("git@").replaceFirst(':', '/')
        url.startsWith("ssh://") -> "https://" + url.removePrefix("ssh://").substringAfter('@', url.removePrefix("ssh://"))
        url.startsWith("git://") -> "https://" + url.removePrefix("git://")
        else -> url
    }
    return url
}

/** `scm:git:` connection string for a normalised https repository URL. */
internal fun scmConnection(httpsUrl: String): String = "scm:git:$httpsUrl.git"

/** `scm:git:ssh://` developer connection string for a normalised https repository URL. */
internal fun scmDeveloperConnection(httpsUrl: String): String =
    "scm:git:ssh://git@${httpsUrl.removePrefix("https://")}.git"
