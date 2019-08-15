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
}