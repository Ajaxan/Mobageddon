import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

val pluginGroup = "com.redfootdev"
val pluginPath = "mobageddon"
val pluginName = "Mobageddon"
val pluginPrefix = "Mobageddon"

val pluginAuthors = listOf("Ajaxan")
val pluginDescription = "This is a plugin to create hordes of dangerous Monsters. It is a spiritual successor combination of both Monster Apocalypse and Blood Moon."

val pluginVersion = "1.0-SNAPSHOT"
val pluginMinecraftVersion = "1.20.1"
val pluginApiVersion = "1.20"

val pluginDependencies = listOf("")
val pluginSoftDependencies = listOf("CoreProtect")

plugins {
    kotlin("jvm") version "1.9.10"
    //id("com.nemosw.copy-jar") version "1.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

version = pluginVersion
group = pluginGroup
val copyJarLocation: String by project

repositories {
    mavenCentral()
    maven(url = "https://papermc.io/repo/repository/maven-public/")
    maven(url = "https://oss.sonatype.org/content/groups/public/")
    maven(url = "https://maven.playpro.com/")
    maven(url = "https://repo.codemc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$pluginMinecraftVersion-R0.1-SNAPSHOT")
    compileOnly("net.coreprotect:coreprotect:21.2")
    compileOnly("com.comphenix.protocol:ProtocolLib-API:4.4.0")
    shadow("org.jetbrains.kotlin:kotlin-stdlib:1.9.10")

    library("com.google.code.gson", "gson", "2.10.1") // All platform plugins
    bukkitLibrary("com.google.code.gson", "gson", "2.10.1") // Bukkit only
    testImplementation(kotlin("test"))
    testImplementation("com.github.seeseemelk:MockBukkit-v1.20:3.9.0")
}

kotlin { // Extension for easy setup
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}


tasks.register<Copy>("copyJarToServer") {
    dependsOn("shadowJar")
    from("build/libs/${project.name}-$pluginVersion-all.jar")
    into(copyJarLocation)
    rename { filename -> filename.replace("-all","") }
}

tasks.test {
    useJUnitPlatform()
}

tasks.build {
    dependsOn("copyJarToServer")
}

application {
    mainClass.set("MainKt")
}
// Github for the guide on this gradle plugin: https://github.com/Minecrell/plugin-yml
bukkit {
    name = pluginName
    version = pluginVersion
    description = pluginDescription
    main = "$pluginGroup.$pluginPath.$pluginName"
    apiVersion = pluginApiVersion

    // Other possible properties from plugin.yml (optional)
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD // or STARTUP
    authors = pluginAuthors
    //depend = pluginDependencies
    softDepend = pluginSoftDependencies
    //loadBefore = listOf("BrokenPlugin")
    prefix = pluginPrefix
    //defaultPermission = BukkitPluginDescription.Permission.Default.OP // TRUE, FALSE, OP or NOT_OP
    //provides = listOf("TestPluginOldName", "TestPlug")

    commands {
        register("example") {
            description = "An Example Command!"
            usage = "/example"
            //aliases = listOf("t")
            //permission = "testplugin.test"
            // permissionMessage = "You may not test this command!"
        }
    }
}

