import org.gradle.internal.impldep.org.fusesource.jansi.AnsiRenderer.test
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    kotlin("jvm") version "1.3.21" apply false
    id("org.jmailen.kotlinter") version "1.20.1"
    id("idea")
}


allprojects {
    group = "io.pleo"
    version = "1.0"

    repositories {
        mavenCentral()
        jcenter()
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.suppressWarnings = true
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

kotlinter {
    continuationIndentSize = 4
}