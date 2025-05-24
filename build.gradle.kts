plugins {
	java
	id("checkstyle")
	jacoco
	id("org.springframework.boot") version "3.3.12"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.sonarqube") version "6.2.0.5505"
	id("io.freefair.lombok") version "8.6"
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-devtools")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	runtimeOnly("com.h2database:h2")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	finalizedBy(tasks.jacocoTestReport)
	useJUnitPlatform()
	testLogging {
		showStandardStreams = true
	}
}

tasks.jacocoTestReport {
	reports {
		xml.required.set(true)
	}
}

sonar {
	properties {
		property("sonar.projectKey", "lagunova-julia_java-project-99")
		property("sonar.organization", "lagunova-julia")
		property("sonar.host.url", "https://sonarcloud.io")
	}
}
