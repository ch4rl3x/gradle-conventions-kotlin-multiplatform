plugins {
    id("de.charlex.convention.android.library")
    id("de.charlex.convention.publishing")
}

android {
    namespace = "de.charlex.testproject.android"
}

mavenPublishConfig {
    name = "test-android-library"
    description = "Test project: plain Android library published through the conventions."
    // Module is named android-library, artifact deliberately is not -- exercises artifactId.
    artifactId = "test-android-library"

    developers {
        developer {
            id = "ch4rl3x"
            name = "Alexander Karkossa"
            email = "alexander.karkossa@googlemail.com"
        }
    }
}
