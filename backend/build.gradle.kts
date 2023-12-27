import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.ir.backend.js.compile

plugins {
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.8.22"
    kotlin("plugin.spring") version "1.8.22"
    kotlin("plugin.jpa") version "1.8.22"
    kotlin("plugin.serialization") version "1.8.22"
}

group = "org.ifmo"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.0")

    // hibernate validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")

    // generate random data
    implementation("io.github.serpro69:kotlin-faker:1.15.0")

    // hibernate ds proxy
    implementation("net.ttddyy:datasource-proxy:1.9")

    // apache csv
    implementation("org.apache.commons:commons-csv:1.10.0")

    // datetime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
    implementation(kotlin("script-runtime"))

    implementation("javax.mail:javax.mail-api:1.6.2")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("org.webjars:bootstrap:5.3.2")
    implementation("org.webjars:webjars-locator-core:0.47")
    implementation("org.webjars:jquery:3.6.0")
}
//
//configurations {
//    developmentOnly
//    runtimeClasspath {
//        extendsFrom(developmentOnly.get())
//    }
//    compileOnly {
//        extendsFrom(configurations.annotationProcessor.get())
//    }
//}
//
tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
