plugins {
    `version-catalog`
    `maven-publish`
    signing
}

catalog {
    versionCatalog {
        from(files(rootProject.file("gradle/libs.versions.toml")))

        // The catalog carries its own version and the plugin aliases, so a
        // consumer names the version once -- in the `from(...)` coordinate.
        version("conventions", project.version.toString())

        listOf(
            "convention-publishing-repository" to "de.charlex.convention.publishing.repository",
            "convention-publishing" to "de.charlex.convention.publishing",
            "convention-jvm-library" to "de.charlex.convention.jvm.library",
            "convention-android-library" to "de.charlex.convention.android.library",
            "convention-kmp-library" to "de.charlex.convention.kmp.library",
        ).forEach { (alias, id) ->
            plugin(alias, id).versionRef("conventions")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("catalog") {
            from(components["versionCatalog"])
        }
    }
}
