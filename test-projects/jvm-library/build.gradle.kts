plugins {
    id("de.charlex.convention.jvm.library")
    id("de.charlex.convention.publishing")
}

mavenPublishConfig {
    name = "test-jvm-library"
    description = "Test project: plain JVM library published through the conventions."
}
