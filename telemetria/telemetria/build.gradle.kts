plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.11"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "1.9.25"
	id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.inteli"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencyManagement {
	imports {
		mavenBom("io.awspring.cloud:spring-cloud-aws-dependencies:3.1.0")
		mavenBom("software.amazon.awssdk:bom:2.25.0")
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("com.squareup.retrofit2:retrofit:2.9.0")
	implementation("com.squareup.retrofit2:converter-gson:2.9.0")
	implementation("com.squareup.okhttp3:okhttp:4.12.0")
	implementation("io.awspring.cloud:spring-cloud-aws-starter-sqs")
	implementation("software.amazon.awssdk:cloudwatch")
	implementation("software.amazon.awssdk:rds")
	implementation("com.amazonaws:aws-lambda-java-core:1.2.3")
	implementation("com.amazonaws:aws-lambda-java-events:3.11.1")
	implementation("org.postgresql:postgresql:42.7.3")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
tasks {
	named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
		archiveClassifier.set("all")
		mergeServiceFiles()
		manifest {
			attributes["Main-Class"] = "com.inteli.telemetria.TelemetriaApplicationKt"
			attributes["Start-Class"] = "com.inteli.telemetria.TelemetriaApplicationKt"
		}
	}
}

