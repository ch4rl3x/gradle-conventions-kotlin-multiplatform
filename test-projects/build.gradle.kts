// AGP and KGP are declared as compileOnly by the convention plugins, so the
// consuming build has to put them on its own buildscript classpath.
plugins {
    alias(conventions.plugins.android.library) apply false
    alias(conventions.plugins.kmp) apply false

    id("de.charlex.convention.publishing.repository")
}

allprojects {
    group = "de.charlex.testproject"
    version = "0.0.0-test"
}
