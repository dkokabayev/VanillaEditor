plugins {
    application
    kotlin("jvm") version "2.1.0"
}

group = "dev.dkokabayev"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("com.formdev:flatlaf:3.5.4")
    testImplementation(kotlin("test"))
}

application {
    this.mainClass = "MainKt"
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "MainKt"
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}