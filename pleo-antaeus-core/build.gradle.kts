plugins {
    kotlin("jvm")
    kotlin("kapt")
}

kotlinProject()

dependencies {
    implementation(project(":pleo-antaeus-data"))
    compile(project(":pleo-antaeus-models"))
    implementation("com.google.dagger:dagger:2.13")
    kapt("com.google.dagger:dagger-compiler:2.13")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0-M2")
    implementation ("it.sauronsoftware.cron4j:cron4j:2.2.5")
}