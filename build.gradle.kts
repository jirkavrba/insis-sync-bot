plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.4.2"
}

version = "0.1"
group = "cz.vse.fis.discord"

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("info.picocli:picocli-codegen")
    annotationProcessor("io.micronaut.serde:micronaut-serde-processor")
    implementation("info.picocli:picocli")
    implementation("io.micrometer:context-propagation")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut.cache:micronaut-cache-caffeine")
    implementation("io.micronaut.picocli:micronaut-picocli")
    implementation("io.micronaut.reactor:micronaut-reactor")
    implementation("io.micronaut.reactor:micronaut-reactor-http-client")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("org.jsoup:jsoup:1.18.1")
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("org.yaml:snakeyaml")
}


application {
    mainClass = "cz.vse.fis.discord.InsisSyncBotCommand"
}
java {
    sourceCompatibility = JavaVersion.toVersion("21")
    targetCompatibility = JavaVersion.toVersion("21")
}



micronaut {
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("cz.vse.fis.discord.*")
    }
}


tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
    jdkVersion = "21"
}


