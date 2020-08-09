package com.masterwok.demosimpletorrentandroid.extensions

import android.content.Context
import android.net.wifi.WifiManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import java.net.InetAddress
import java.net.UnknownHostException


/**
 * Set the support of change animations. By default this value is true. Setting
 * this value to false will prevent the, "dim" that occurs when an item is updated.
 */
fun RecyclerView.setSupportsChangeAnimations(doesSupport: Boolean) {
    (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = doesSupport
}


@Throws(UnknownHostException::class)
fun Context.getIpAddress(): InetAddress? {
    val wifiMgr = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val wifiInfo = wifiMgr.connectionInfo
    val ip = wifiInfo.ipAddress
    return if (ip == 0) {
        null
    } else {
        val ipAddress = convertIpAddress(ip)
        InetAddress.getByAddress(ipAddress)
    }
}

private fun convertIpAddress(ip: Int): ByteArray {
    return byteArrayOf(
            (ip and 0xFF).toByte(),
            (ip shr 8 and 0xFF).toByte(),
            (ip shr 16 and 0xFF).toByte(),
            (ip shr 24 and 0xFF).toByte())
}