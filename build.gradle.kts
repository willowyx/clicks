import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.1.21"
    application
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
}

group = "dev.willowyx"
version = "0.2.0"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

val lwjglVersion = "3.3.6"
val os = org.gradle.internal.os.OperatingSystem.current()

fun lwjglNatives(os: org.gradle.internal.os.OperatingSystem) = when {
    os.isWindows -> "natives-windows"
    os.isLinux -> "natives-linux"
    os.isMacOsX -> "natives-macos"
    else -> error("Unsupported OS")
}

dependencies {
    implementation("com.github.SpaiR.imgui-java:imgui-java-app:v1.89.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    implementation("org.lwjgl:lwjgl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-opengl:$lwjglVersion")

    runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:${lwjglNatives(os)}")
    runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglVersion:${lwjglNatives(os)}")
    runtimeOnly("org.lwjgl:lwjgl-opengl:$lwjglVersion:${lwjglNatives(os)}")

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("Main")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register("generateVersionProperties") {
    val outputDir = layout.buildDirectory.dir("generated/resources/version")
    outputs.dir(outputDir)

    doLast {
        val versionFile = outputDir.get().file("version.properties").asFile
        versionFile.parentFile.mkdirs()
        versionFile.writeText("version=${project.version}\n")
    }
}

sourceSets.main {
    resources.srcDir(tasks.named("generateVersionProperties"))
}
