plugins {
    `java-library`
    `maven-publish`
    signing
    id("io.codearte.nexus-staging") version "0.30.0"
    id("org.cadixdev.licenser") version "0.6.1"
}

base.archivesName(project.name.toLowerCase())

sourceSets.create("java11") {
    java.srcDir("src/main/java11")
    java.srcDir("src/main/java")
}

sourceSets.create("intTest")

configurations["java11CompileClasspath"].extendsFrom(configurations.compileClasspath.get())
configurations["intTestImplementation"].extendsFrom(configurations.api.get())

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.apache.logging.log4j:log4j-core:2.14.1")
    annotationProcessor("org.apache.logging.log4j:log4j-core:2.14.1")

    api("org.jline:jline-reader:3.20.0")

    compileOnly("org.checkerframework:checker-qual:3.17.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    "intTestImplementation"(files(tasks.named("jar")))
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "net.minecrell.terminalconsole")
        attributes("Multi-Release" to "true")
    }
    into("META-INF/versions/11") {
        from(sourceSets["java11"].output)
        include("module-info.class")
    }
}

tasks.named<JavaCompile>("compileJava") {
    options.release.set(8)
}

tasks.named<JavaCompile>("compileJava11Java") {
    options.release.set(11)
    options.javaModuleVersion.set(project.version as String)
}

tasks.named<JavaCompile>("compileIntTestJava") {
    options.release.set(11)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.check { dependsOn(tasks.named("compileIntTestJava")) }

val isSnapshot = version.toString().endsWith("-SNAPSHOT")

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = project.name.toLowerCase()

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
