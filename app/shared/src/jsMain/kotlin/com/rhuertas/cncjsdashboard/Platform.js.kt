package com.rhuertas.cncjsdashboard

import web.navigator.navigator

class JsPlatform : Platform {
    private val userAgent = navigator.userAgent
    private val browserList = listOf("Chrome", "Firefox", "Safari", "Edge")

    override val name: String = userAgent.findAnyOf(browserList, ignoreCase = true)
        ?.let { (startIndex) -> userAgent.substring(startIndex).substringBefore(" ") }
        ?: "Unknown"
}

actual fun getPlatform(): Platform = JsPlatform()

actual suspend fun getSystemMetrics(): SystemMetrics {
    // JavaScript/Browser environment has limited access to system metrics
    // Simulate realistic system-level metrics for demonstration
    // Typical modern system: 8 cores, 16GB RAM
    
    val numCores = 8
    val cpuCores = (0 until numCores).map { core ->
        CpuCoreUsage(
            coreIndex = core,
            usage = (Math.random() * 60 + 10).toFloat() // 10-70% realistic range
        )
    }
    
    // Simulate typical system: 16GB total, vary between 6-12GB used
    val ramUsedMb = (6144 + Math.random() * 6144).toLong()
    val ramTotalMb = 16384
    
    return SystemMetrics(
        cpuCores = cpuCores,
        ramUsedMb = ramUsedMb,
        ramTotalMb = ramTotalMb
    )
}