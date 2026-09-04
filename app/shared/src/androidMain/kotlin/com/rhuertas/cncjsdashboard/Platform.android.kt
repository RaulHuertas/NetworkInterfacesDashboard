package com.rhuertas.cncjsdashboard

import android.app.ActivityManager
import android.content.Context
import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual suspend fun getSystemMetrics(): SystemMetrics {
    // Android: Try to get system-level metrics
    // Mock realistic Android device: 8 cores, 6GB RAM
    
    val numCores = Runtime.getRuntime().availableProcessors()
    val cpuCores = (0 until numCores).map { core ->
        CpuCoreUsage(
            coreIndex = core,
            usage = (Math.random() * 50 + 15).toFloat() // 15-65% realistic for mobile
        )
    }
    
    val runtime = Runtime.getRuntime()
    val maxMemory = runtime.maxMemory() / (1024 * 1024)
    val totalMemory = runtime.totalMemory() / (1024 * 1024)
    val freeMemory = runtime.freeMemory() / (1024 * 1024)
    val usedMemory = totalMemory - freeMemory
    
    // If max memory is very small (only heap), estimate system RAM at ~3-4x
    val estimatedSystemRam = if (maxMemory < 1024) maxMemory * 3 else maxMemory
    
    return SystemMetrics(
        cpuCores = cpuCores,
        ramUsedMb = usedMemory.coerceAtMost(estimatedSystemRam),
        ramTotalMb = estimatedSystemRam
    )
}