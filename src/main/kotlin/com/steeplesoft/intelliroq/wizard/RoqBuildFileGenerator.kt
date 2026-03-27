package com.steeplesoft.intelliroq.wizard

import com.steeplesoft.intelliroq.services.RoqPluginManager

/**
 * Utility for generating Maven and Gradle build files for Roq projects.
 */
object RoqBuildFileGenerator {

    const val DEFAULT_QUARKUS_VERSION = "3.34.1"
    const val DEFAULT_ROQ_VERSION = "2.0.5"

    /**
     * Generates a complete Maven pom.xml with Roq dependencies.
     */
    fun generateMavenPom(
        projectName: String,
        selectedPlugins: Set<String>,
        quarkusVersion: String = DEFAULT_QUARKUS_VERSION,
        roqVersion: String = DEFAULT_ROQ_VERSION
    ): String {
        val pluginDependencies = selectedPlugins
            .filter { it != "markdown" } // markdown is included in core
            .joinToString("\n") { pluginName ->
                val artifactId = "${RoqPluginManager.ROQ_PLUGIN_PREFIX}$pluginName"
                """        <dependency>
            <groupId>${RoqPluginManager.ROQ_GROUP_ID}</groupId>
            <artifactId>$artifactId</artifactId>
            <version>${"$"}{roq.version}</version>
        </dependency>"""
            }

        return """<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>$projectName</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <quarkus.version>$quarkusVersion</quarkus.version>
        <roq.version>$roqVersion</roq.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>io.quarkus.platform</groupId>
                <artifactId>quarkus-bom</artifactId>
                <version>${"$"}{quarkus.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>${RoqPluginManager.ROQ_GROUP_ID}</groupId>
            <artifactId>${RoqPluginManager.ROQ_CORE_ARTIFACT}</artifactId>
            <version>${"$"}{roq.version}</version>
        </dependency>
${if (pluginDependencies.isNotEmpty()) pluginDependencies else ""}
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>io.quarkus.platform</groupId>
                <artifactId>quarkus-maven-plugin</artifactId>
                <version>${"$"}{quarkus.version}</version>
                <extensions>true</extensions>
                <executions>
                    <execution>
                        <goals>
                            <goal>build</goal>
                            <goal>generate-code</goal>
                            <goal>generate-code-tests</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.15.0</version>
                <configuration>
                    <release>21</release>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
"""
    }

    /**
     * Generates a Gradle build.gradle.kts file with Roq dependencies.
     */
    fun generateGradleBuild(
        selectedPlugins: Set<String>,
        quarkusVersion: String = DEFAULT_QUARKUS_VERSION,
        roqVersion: String = DEFAULT_ROQ_VERSION
    ): String {
        val pluginDependencies = selectedPlugins
            .filter { it != "markdown" } // markdown is included in core
            .joinToString("\n") { pluginName ->
                val artifactId = "${RoqPluginManager.ROQ_PLUGIN_PREFIX}$pluginName"
                """    implementation("${RoqPluginManager.ROQ_GROUP_ID}:$artifactId:$roqVersion")"""
            }

        return """plugins {
    java
    id("io.quarkus") version "$quarkusVersion"
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("${"$"}{quarkusPlatformGroupId}:${"$"}{quarkusPlatformArtifactId}:${"$"}{quarkusPlatformVersion}"))
    implementation("${RoqPluginManager.ROQ_GROUP_ID}:${RoqPluginManager.ROQ_CORE_ARTIFACT}:$roqVersion")
${if (pluginDependencies.isNotEmpty()) pluginDependencies else ""}
}

group = "com.example"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
"""
    }

    /**
     * Generates a Gradle settings.gradle.kts file.
     */
    fun generateGradleSettings(
        projectName: String,
        quarkusVersion: String = DEFAULT_QUARKUS_VERSION
    ): String {
        return """pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
        id("io.quarkus") version "$quarkusVersion"
    }
}

rootProject.name = "$projectName"
"""
    }

    /**
     * Generates a gradle.properties file with Quarkus platform properties.
     */
    fun generateGradleProperties(quarkusVersion: String = DEFAULT_QUARKUS_VERSION): String {
        return """quarkusPlatformGroupId=io.quarkus.platform
quarkusPlatformArtifactId=quarkus-bom
quarkusPlatformVersion=$quarkusVersion
"""
    }
}
