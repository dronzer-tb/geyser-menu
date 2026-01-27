plugins {
    java
    id("com.gradleup.shadow") version "9.0.0-beta4"
}

group = "com.geysermenu"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven("https://repo.opencollab.dev/main/")
    maven("https://jitpack.io")
    maven("https://maven.lenni0451.net/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    // Geyser Core (for internal packet translators and registries)
    compileOnly("org.geysermc.geyser:core:2.9.1-SNAPSHOT")

    // Cumulus (Forms) - usually bundled with Geyser
    compileOnly("org.geysermc.cumulus:cumulus:1.1.2")

    // Floodgate API (optional fallback)
    compileOnly("org.geysermc.floodgate:api:2.2.3-SNAPSHOT")

    // Netty for TCP server
    implementation("io.netty:netty-all:4.1.100.Final")

    // JSON for protocol
    implementation("com.google.code.gson:gson:2.10.1")

    // YAML processing for config
    implementation("org.yaml:snakeyaml:2.2")

    // Annotations
    compileOnly("org.checkerframework:checker-qual:3.42.0")
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("GeyserMenu.jar")
    relocate("io.netty", "com.geysermenu.libs.netty")
    relocate("com.google.gson", "com.geysermenu.libs.gson")
    relocate("org.yaml.snakeyaml", "com.geysermenu.libs.snakeyaml")

    dependencies {
        exclude(dependency("org.geysermc.geyser:.*"))
        exclude(dependency("org.geysermc.cumulus:.*"))
        exclude(dependency("org.geysermc.event:.*"))
        exclude(dependency("org.geysermc.floodgate:.*"))
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
