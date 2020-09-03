plugins {
    java
    `maven-publish`
    signing
    id("io.codearte.nexus-staging") version "0.21.0"
    id("net.minecrell.licenser") version "0.4.1"
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
    compile("org.apache.logging.log4j:log4j-core:2.8.1")
    annotationProcessor("org.apache.logging.log4j:log4j-core:2.8.1")

    compile("org.jline:jline-reader:3.12.1")

    compileOnly("org.checkerframework:checker-qual:2.9.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.1")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.5.1")
}
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
tasks.withType<Test> {
    useJUnitPlatform()
}

val sourceJar = task<Jar>("sourceJar") {
    archiveClassifier("sources")
    from(sourceSets["main"].allSource)
}

val javadocJar = task<Jar>("javadocJar") {
    archiveClassifier("javadoc")
    from(tasks["javadoc"])
}

val isSnapshot = version.toString().endsWith("-SNAPSHOT")

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = base.archivesBaseName

            artifact(sourceJar)
            artifact(javadocJar)

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
