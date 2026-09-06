package de.charlex.convention

import org.gradle.api.Project
import java.io.File

internal fun Project.originUrl(): String? {
    val gitPath = generateSequence(rootDir) { it.parentFile }
        .map { File(it, ".git") }
        .firstOrNull { it.exists() }
        ?: return null

    val config = File(resolveGitDir(gitPath) ?: return null, "config")
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

private fun resolveGitDir(gitPath: File): File? {
    if (gitPath.isDirectory) return gitPath

    // A worktree or submodule stores `gitdir: <path>` in a file instead.
    val target = gitPath.readLines()
        .firstOrNull { it.startsWith("gitdir:") }
        ?.substringAfter(':')
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: return null

    val linked = File(target).let { if (it.isAbsolute) it else File(gitPath.parentFile, target) }
    if (File(linked, "config").isFile) return linked

    // .git/worktrees/<name> -> .git
    return linked.parentFile?.parentFile?.takeIf { File(it, "config").isFile }
}

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

internal fun scmConnection(httpsUrl: String): String = "scm:git:$httpsUrl.git"

internal fun scmDeveloperConnection(httpsUrl: String): String =
    "scm:git:ssh://git@${httpsUrl.removePrefix("https://")}.git"
