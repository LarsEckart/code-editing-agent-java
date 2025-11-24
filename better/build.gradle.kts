plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.anthropic.java)
    implementation("org.slf4j:slf4j-simple:2.0.16")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass = "com.larseckart.better.BetterApp"
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

tasks.named<JavaExec>("run") {
    systemProperties = System.getProperties().toMap() as Map<String, Any>
    standardInput = System.`in`
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}
