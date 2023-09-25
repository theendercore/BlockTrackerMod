import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("fabric-loom") version "1.3.8"
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    id("org.teamvoided.iridium") version "3.0.2"
}

group = project.properties["maven_group"]!!
version = project.properties["mod_version"]!!
base.archivesName.set(project.properties["archives_base_name"] as String)
description = "stat_tracker Description"

repositories {
    mavenCentral()
    maven {
        name = "brokenfuseReleases"
        url = uri("https://maven.teamvoided.org/releases")
    }
}

modSettings {
    modId(base.archivesName.get())
    modName("StatTracker")

    entrypoint("main", "org.teamvoided.stat_tracker.StatTracker::commonInit")
    entrypoint("client", "org.teamvoided.stat_tracker.StatTracker::clientInit")

    mixinFile("stat_tracker.mixins.json")
}
dependencies {
    modImplementation("org.teamvoided:voidlib-core:1.5.6+1.20.1")
    modImplementation("org.teamvoided:voidlib-vui:1.5.6+1.20.1")
//    modImplementation("org.teamvoided:voidlib-config:1.5.4+1.20.1")
}

tasks {
    val targetJavaVersion = 17
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(targetJavaVersion)
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = targetJavaVersion.toString()
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(JavaVersion.toVersion(targetJavaVersion).toString()))
        withSourcesJar()
    }
}