import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

plugins {
    application
    kotlin("jvm")
    kotlin("kapt")
}

kotlinProject()

dataLibs()

application {
    mainClassName = "io.pleo.antaeus.app.AntaeusApp"
}

dependencies {
    implementation(project(":pleo-antaeus-data"))
    implementation(project(":pleo-antaeus-rest"))
    implementation(project(":pleo-antaeus-core"))
    compile(project(":pleo-antaeus-models"))
    implementation("com.google.dagger:dagger:2.13")
    kapt("com.google.dagger:dagger-compiler:2.13")
    implementation ("it.sauronsoftware.cron4j:cron4j:2.2.5")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0-M2")
}