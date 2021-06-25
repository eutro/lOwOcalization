plugins {
    id("fabric-loom") version "0.8-SNAPSHOT"
    id("maven-publish")
}

base {
    archivesBaseName = properties["archives_base_name"] as String
    version = "${properties["minecraft_version"]}-${properties["mod_version"]}-fabric"
    group = properties["maven_group"]!!
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
    mappings("net.fabricmc:yarn:${properties["yarn_mappings"]}:v2")
    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")

    compileOnly("org.jetbrains:annotations:19.0.0")
}

tasks.processResources {
    inputs.property("version", properties["mod_version"])
    filesMatching("fabric.mod.json") {
        expand("version" to properties["mod_version"])
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.jar {
    from("LICENSE")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            // add all the jars that should be included when publishing to maven
            artifact(tasks["remapJar"]) {
                builtBy(tasks["remapJar"])
            }
            artifact(tasks["sourcesJar"]) {
                builtBy(tasks["remapSourcesJar"])
            }
        }
    }

    repositories {
    }
}
