import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.declarative.dsl.schema.FqName.Empty.packageName
import org.gradle.jvm.tasks.Jar
import sun.jvmstat.monitor.MonitoredVmUtil.mainClass
import java.lang.module.ModuleFinder.compose

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val desktopPackageVersion = "1.0.0"

dependencies {
    implementation(project(":app:shared"))

    implementation(libs.compose.desktop.jvm)

    implementation(libs.skiko.awt.runtime.windows)
    implementation(libs.skiko.awt.runtime.linux.x64)
    implementation(libs.skiko.awt.runtime.linux.arm64)
    implementation(libs.skiko.awt.runtime.macos.x64)
    implementation(libs.skiko.awt.runtime.macos.arm64)

    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "com.rhuertas.cncjsdashboard.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.rhuertas.cncjsdashboard"
            packageVersion = desktopPackageVersion
        }
    }
}

tasks.register<Jar>("packageDesktopJar") {
    group = "distribution"
    description = "Packages a single runnable desktop JAR for all supported Java desktop platforms."

    archiveBaseName = "com.rhuertas.cncjsdashboard-desktop"
    archiveVersion = desktopPackageVersion
    archiveClassifier = "all"

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest.attributes["Main-Class"] = "com.rhuertas.cncjsdashboard.MainKt"

    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath, tasks.named("jar"))
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith(".jar") }
            .map { zipTree(it) }
    })
}