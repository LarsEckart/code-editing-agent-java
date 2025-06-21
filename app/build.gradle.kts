plugins {
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "6.25.0"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.anthropic:anthropic-java:2.0.0")
    implementation(libs.google.genai)
    
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

application {
    mainClass = "com.larseckart.App"
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

tasks.named<JavaExec>("run") {
    systemProperties = System.getProperties().toMap() as Map<String, Any>
    standardInput = System.`in`
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

spotless {
    java {
        googleJavaFormat()
    }
}
