plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(libs.guava)
    implementation(libs.jackson.databind)
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "com.larseckart.App"
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

// Set the Main-Class attribute in the jar manifest

tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

// Fat JAR task to include all dependencies

tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) }
    })
}

tasks.named("build") {
    dependsOn("fatJar")
}
