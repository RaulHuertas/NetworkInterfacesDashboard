package com.rhuertas.cncjsdashboard

import java.lang.management.ManagementFactory
import java.lang.management.OperatingSystemMXBean
import java.io.File

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual suspend fun getSystemMetrics(): SystemMetrics {
    val osBean = ManagementFactory.getOperatingSystemMXBean()
    
    // Get system-level CPU usage per core
    val cpuCores = getSystemCpuUsage(osBean.availableProcessors)
    
    // Get system-level memory (not JVM heap)
    val totalMemoryBytes = getTotalSystemMemory(osBean)
    val usedMemoryBytes = getUsedSystemMemory(osBean)
    
    return SystemMetrics(
        cpuCores = cpuCores,
        ramUsedMb = usedMemoryBytes / (1024 * 1024),
        ramTotalMb = totalMemoryBytes / (1024 * 1024)
    )
}

private fun getSystemCpuUsage(numCores: Int): List<CpuCoreUsage> {
    val osBean = ManagementFactory.getOperatingSystemMXBean()
    
    // Try to read from /proc/stat on Linux
    val procStatFile = File("/proc/stat")
    if (procStatFile.exists()) {
        return readLinuxCpuUsage(numCores, procStatFile)
    }
    
    // Fallback: use system load average as approximation for all cores
    val systemLoad = osBean.systemLoadAverage.coerceIn(0.0, numCores.toDouble())
    val avgUsagePercent = (systemLoad / numCores * 100).toFloat().coerceIn(0f, 100f)
    
    return (0 until numCores).map { core ->
        CpuCoreUsage(
            coreIndex = core,
            usage = avgUsagePercent + (Math.random() * 10 - 5).toFloat().coerceIn(-avgUsagePercent, 100f - avgUsagePercent)
        )
    }
}

private fun readLinuxCpuUsage(numCores: Int, procStatFile: File): List<CpuCoreUsage> {
    return try {
        val lines = procStatFile.readLines()
        val cpuCores = mutableListOf<CpuCoreUsage>()
        
        for (coreIndex in 0 until numCores) {
            val cpuLine = lines.find { it.startsWith("cpu$coreIndex ") }
            if (cpuLine != null) {
                val usage = calculateCpuUsageFromProc(cpuLine)
                cpuCores.add(CpuCoreUsage(coreIndex, usage))
            } else {
                // Core not found, use 0%
                cpuCores.add(CpuCoreUsage(coreIndex, 0f))
            }
        }
        
        cpuCores
    } catch (e: Exception) {
        // Fallback if parsing fails
        (0 until numCores).map { core ->
            CpuCoreUsage(coreIndex = core, usage = 0f)
        }
    }
}

private fun calculateCpuUsageFromProc(cpuLine: String): Float {
    return try {
        val parts = cpuLine.split(Regex("\\s+"))
        if (parts.size < 5) return 0f
        
        val user = parts[1].toLong()
        val nice = parts[2].toLong()
        val system = parts[3].toLong()
        val idle = parts[4].toLong()
        
        val total = user + nice + system + idle
        if (total == 0L) return 0f
        
        val busy = user + nice + system
        ((busy.toFloat() / total) * 100).coerceIn(0f, 100f)
    } catch (e: Exception) {
        0f
    }
}

private fun getTotalSystemMemory(osBean: OperatingSystemMXBean): Long {
    return try {
        // Use com.sun.management.OperatingSystemMXBean if available
        val totalMemMethod = osBean.javaClass.getMethod("getTotalPhysicalMemorySize")
        totalMemMethod.invoke(osBean) as Long
    } catch (e: Exception) {
        // Fallback: use JVM max memory if system API not available
        Runtime.getRuntime().maxMemory()
    }
}

private fun getUsedSystemMemory(osBean: OperatingSystemMXBean): Long {
    return try {
        val totalMemMethod = osBean.javaClass.getMethod("getTotalPhysicalMemorySize")
        val freeMemMethod = osBean.javaClass.getMethod("getFreePhysicalMemorySize")
        
        val totalMem = totalMemMethod.invoke(osBean) as Long
        val freeMem = freeMemMethod.invoke(osBean) as Long
        
        totalMem - freeMem
    } catch (e: Exception) {
        // Fallback: use JVM memory stats
        val runtime = Runtime.getRuntime()
        val totalMemoryBytes = runtime.totalMemory()
        val freeMemoryBytes = runtime.freeMemory()
        totalMemoryBytes - freeMemoryBytes
    }
}