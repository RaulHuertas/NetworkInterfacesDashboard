package com.rhuertas.cncjsdashboard

import java.net.NetworkInterface

actual fun getNetworkInterfaces(): List<NetworkInterfaceInfo> {
    return NetworkInterface.getNetworkInterfaces()
        ?.asSequence()
        ?.filter { !it.isLoopback && it.isUp }
        ?.flatMap { iface ->
            iface.interfaceAddresses.map { addr ->
                val ip = addr.address.hostAddress ?: "N/A"
                val netmask = prefixLengthToNetmask(addr.networkPrefixLength)
                val mac = iface.hardwareAddress
                    ?.joinToString(":") { byte -> "%02X".format(byte) }
                    ?: "N/A"
                NetworkInterfaceInfo(
                    name = iface.name,
                    ip = ip,
                    netmask = netmask,
                    mac = mac,
                )
            }
        }
        ?.toList()
        ?: emptyList()
}

private fun prefixLengthToNetmask(prefixLength: Short): String {
    if (prefixLength <= 0) return "N/A"
    val bits = 0xFFFFFFFF.toInt() shl (32 - prefixLength)
    return "${(bits shr 24) and 0xFF}.${(bits shr 16) and 0xFF}.${(bits shr 8) and 0xFF}.${bits and 0xFF}"
}
