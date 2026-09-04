package com.rhuertas.cncjsdashboard

import platform.UIKit.UIDevice
import kotlin.math.min

class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual suspend fun getSystemMetrics(): SystemMetrics {
    // iOS has limited access to system metrics
    // Simulate realistic iOS device: 6 cores, 4GB RAM
    
    val availableProcessors = min(6, Runtime.getRuntime().availableProcessors())
    val cpuCores = (0 until availableProcessors).map { core ->
        CpuCoreUsage(
            coreIndex = core,
            usage = (Math.random() * 40 + 10).toFloat() // 10-50% realistic for mobile
        )
    }
    
    val runtime = Runtime.getRuntime()
    val maxMemory = runtime.maxMemory() / (1024 * 1024)
    val totalMemory = runtime.totalMemory() / (1024 * 1024)
    val freeMemory = runtime.freeMemory() / (1024 * 1024)
    val usedMemory = totalMemory - freeMemory
    
    // iOS typically has 3-6GB RAM, estimate based on available
    val estimatedSystemRam = if (maxMemory < 1024) maxMemory * 2 else maxMemory
    
    return SystemMetrics(
        cpuCores = cpuCores,
        ramUsedMb = usedMemory.coerceAtMost(estimatedSystemRam),
        ramTotalMb = estimatedSystemRam
    )
}