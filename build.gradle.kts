import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    application
}

group = "me.home"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.+")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.+")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}

application {
    mainClass.set("MainKt")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "14"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "14"
}