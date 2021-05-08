plugins {
    `java-library`
    `maven-publish`
    signing
    id("io.codearte.nexus-staging") version "0.30.0"
    id("org.cadixdev.licenser") version "0.6.0"
}

val artifactId = project.name.toLowerCase()
base.archivesBaseName = artifactId

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.apache.logging.log4j:log4j-core:2.8.1")
    annotationProcessor("org.apache.logging.log4j:log4j-core:2.8.1")

    api("org.jline:jline-reader:3.20.0")

    compileOnly("org.checkerframework:checker-qual:3.13.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val isSnapshot = version.toString().endsWith("-SNAPSHOT")

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = base.archivesBaseName

            pom {
                val url: String by project
                name(project.name)
                description(project.description!!)
                url(url)

                scm {
                    url(url)
                    connection("scm:git:$url.git")
                    developerConnection.set(connection)
                }

                issueManagement {
                    system("GitHub Issues")
                    url("$url/issues")
                }

                developers {
                    developer {
                        id("minecrell")
                        name("Minecrell")
                        email("minecrell@minecrell.net")
                    }
                }

                licenses {
                    license {
                        name("MIT License")
                        url("https://opensource.org/licenses/MIT")
                        distribution("repo")
                    }
                }
            }
        }
    }

    repositories {
        val sonatypeUsername: String? by project
        val sonatypePassword: String? by project
        if (sonatypeUsername != null && sonatypePassword != null) {
            val url = if (isSnapshot) "https://oss.sonatype.org/content/repositories/snapshots/"
            else "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            maven(url) {
                credentials {
                    username = sonatypeUsername
                    password = sonatypePassword
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

tasks.withType<Sign> {
    onlyIf { !isSnapshot }
}

operator fun Property<String>.invoke(v: String) = set(v)
