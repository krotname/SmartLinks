plugins {
    java
    jacoco
    id("org.springframework.boot") version "4.1.0"
    id("io.gatling.gradle") version "3.13.5"
}

group = "name.krot"
version = "0.0.1-SNAPSHOT"

val springBootBom = "org.springframework.boot:spring-boot-dependencies:4.1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

jacoco {
    toolVersion = "0.8.15"
    reportsDirectory = layout.buildDirectory.dir("JacocoReportDir")
}

tasks.jacocoTestReport {
    reports {
        xml.required = false
        csv.required = true
        csv.outputLocation = layout.buildDirectory.file("JacocoReportDir/jacoco.csv")
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(springBootBom))
    developmentOnly(platform(springBootBom))
    testImplementation(platform(springBootBom))
    testRuntimeOnly(platform(springBootBom))

    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.apache.commons:commons-pool2")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-data-redis-test")
    testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.5"))
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Gatling run tasks — bypasses GatlingRunTask which uses removed Gradle 9 API (project.reportsDir)
val gatlingResultsDir = layout.buildDirectory.dir("gatling-results")

listOf("SmokeSim", "LoadSim", "StressSim", "SpikeSim", "BreakSim").forEach { sim ->
    tasks.register<JavaExec>("gatlingRun$sim") {
        group = "gatling"
        description = "Run Gatling simulation: $sim"
        dependsOn("gatlingClasses")

        classpath = sourceSets["gatling"].runtimeClasspath
        mainClass.set("io.gatling.app.Gatling")

        args(
            "--simulation", "name.krot.smartlinks.load.$sim",
            "--results-folder", gatlingResultsDir.get().asFile.absolutePath
        )

        systemProperties(
            mapOf("baseUrl" to (System.getProperty("baseUrl") ?: "http://localhost:8090"))
        )

        jvmArgs(
            "-Xms512m", "-Xmx2g", "-XX:+UseG1GC",
            "--add-opens", "java.base/java.lang=ALL-UNNAMED",
            "--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED",
            "--add-opens", "java.base/java.util=ALL-UNNAMED",
            "--add-opens", "java.base/sun.util.calendar=ALL-UNNAMED"
        )
    }
}
