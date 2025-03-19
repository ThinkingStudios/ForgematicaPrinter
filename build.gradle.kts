plugins {
    id("dev.architectury.loom").version("1.9-SNAPSHOT")
    id("com.hypherionmc.modutils.modpublisher").version("2.+")
    id("maven-publish")
}

val minecraft_version: String by project
val mappings_version: String by project
val yarn_patch: String by project
val neoforge_version: String by project
val mafglib_version: String by project
val forgematica_version: String by project

val archives_base_name: String by project
val mod_version: String by project

base.archivesName = archives_base_name
version = "${mod_version}+mc${minecraft_version}"

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://masa.dy.fi/maven")
    //maven("https://www.cursemaven.com")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://jitpack.io")
    maven("https://maven.neoforged.net/releases")
    maven("https://api.modrinth.com/maven")
    maven {
        url = uri("https://maven2.bai.lol")
        content {
            includeGroup("lol.bai")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraft_version}")
    mappings(loom.layered {
        mappings("net.fabricmc:yarn:${mappings_version}:v2")
        mappings("dev.architectury:yarn-mappings-patch-neoforge:${yarn_patch}")
    })
    implementation("com.google.code.findbugs:jsr305:3.0.2")

    neoForge("net.neoforged:neoforge:${neoforge_version}")

    modImplementation("maven.modrinth:mafglib:${mafglib_version}")
    modImplementation("maven.modrinth:forgematica:${forgematica_version}")

    modLocalRuntime("lol.bai:badpackets:neo-0.7.3")
}

tasks.withType<ProcessResources> {
    inputs.property("version", version)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(mapOf("version" to version))
    }
}

publisher {
    apiKeys.modrinth(System.getenv("MODRINTH_TOKEN"))
    apiKeys.curseforge(System.getenv("CURSEFORGE_TOKEN"))

    curseID.set("$project.curseforge_id")
    modrinthID.set("$project.modrinth_id")
    versionType.set("release")
    changelog.set(file("CHANGELOG.md"))
    displayName.set("$project.version")
    gameVersions.set(listOf("1.20.5", "1.20.6"))
    loaders.set(listOf(loom.platform.get().id()))
    projectVersion.set("$project.version")
    artifact.set(tasks.remapJar)
    addAdditionalFile(tasks.remapSourcesJar)
}
