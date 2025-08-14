plugins {
	java
	id("checkstyle")
	jacoco
	id("org.springframework.boot") version "3.3.12"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.sonarqube") version "6.2.0.5505"
	id("io.freefair.lombok") version "8.6"
	id ("io.sentry.jvm.gradle") version "5.9.0"
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

sentry {
	includeSourceContext = true
	org = "julia-ufimtseva"
	projectName = "java-spring-boot"
	authToken = System.getenv("SENTRY_AUTH_TOKEN")
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
	implementation("org.openapitools:jackson-databind-nullable:0.2.6")

	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-devtools")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.postgresql:postgresql:42.7.2")
	runtimeOnly("com.h2database:h2")

	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

	implementation("net.datafaker:datafaker:2.0.1")
	implementation("org.instancio:instancio-junit:3.3.0")

	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
	implementation("io.sentry:sentry-opentelemetry-agent:8.19.1")
//	implementation("io.sentry:sentry-spring-boot-starter-jakarta:7.11.1")

	testImplementation(platform("org.junit:junit-bom:5.10.0"))
	testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("net.javacrumbs.json-unit:json-unit-assertj:3.2.2")
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

tasks.register<Copy>("copySentryAgent") {
	dependsOn(configurations.runtimeClasspath)

	from({
		configurations.runtimeClasspath.get()
				.filter { it.name.startsWith("sentry-opentelemetry-agent") }
				.also {
					if (it.isEmpty()) {
						throw GradleException("Sentry agent JAR not found in dependencies!")
					}
				}
	})

	into(layout.buildDirectory.dir("libs"))
}

// 3. Привязываем копирование к сборке JAR
tasks.named("bootJar") {
	dependsOn("copySentryAgent")
}

// 4. Для запуска через bootRun (опционально)
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
	jvmArgs = listOf(
			"-javaagent:${layout.buildDirectory.get().asFile}/libs/sentry-opentelemetry-agent.jar"
	)
}
