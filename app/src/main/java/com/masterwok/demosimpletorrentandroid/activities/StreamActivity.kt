package com.masterwok.demosimpletorrentandroid.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.crazylegend.kotlinextensions.log.debug
import com.crazylegend.kotlinextensions.views.setOnClickListenerCooldown
import com.masterwok.demosimpletorrentandroid.R
import com.masterwok.demosimpletorrentandroid.extensions.getIpAddress
import com.masterwok.simpletorrentandroid.streamer.StreamStatus
import com.masterwok.simpletorrentandroid.streamer.Torrent
import com.masterwok.simpletorrentandroid.streamer.TorrentOptions
import com.masterwok.simpletorrentandroid.streamer.streamServer.TorrentServerListener
import com.masterwok.simpletorrentandroid.streamer.streamServer.TorrentStreamNotInitializedException
import com.masterwok.simpletorrentandroid.streamer.streamServer.TorrentStreamServer
import kotlinx.android.synthetic.main.activity_test.*
import java.io.IOException
import java.net.UnknownHostException


/**
 * Created by crazy on 8/9/20 to long live and prosper !
 */
class StreamActivity : AppCompatActivity(R.layout.activity_test), TorrentServerListener {


    private val link1 = "magnet:?xt=urn:btih:a54926c2e07b0e5f0243954330b599b31c804f0b&dn=Batman%20The%20Dark%20Knight%20(2008)%20%5b1080p%5d&tr=udp%3a%2f%2fopen.demonii.com%3a1337&tr=udp%3a%2f%2ftracker.coppersurfer.tk%3a6969&tr=udp%3a%2f%2ftracker.leechers-paradise.org%3a6969&tr=udp%3a%2f%2ftracker.pomf.se%3a80&tr=udp%3a%2f%2ftracker.publicbt.com%3a80&tr=udp%3a%2f%2ftracker.openbittorrent.com%3a80&tr=udp%3a%2f%2ftracker.istole.it%3a80"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val torrentOptions: TorrentOptions = TorrentOptions.Builder()
                .saveLocation(filesDir)
                .removeFilesAfterStop(true)
                .anonymousMode(true)
                .build()

        var ipAddress = "127.0.0.1"
        try {
            val inetAddress = getIpAddress()
            if (inetAddress != null) {
                ipAddress = inetAddress.hostAddress
            }
        } catch (e: UnknownHostException) {
            e.printStackTrace()
        }

        val torrentStreamServer = TorrentStreamServer.getInstance()
        torrentStreamServer.setTorrentOptions(torrentOptions)
        torrentStreamServer.setServerHost(ipAddress)
        torrentStreamServer.setServerPort(8080)
        torrentStreamServer.startTorrentStream()
        torrentStreamServer.addListener(this)

        try {
            torrentStreamServer.startStream(link1)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show()
        } catch (e: TorrentStreamNotInitializedException) {
            e.printStackTrace()
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show()
        }

        testButton.setOnClickListenerCooldown {
            torrentStreamServer.stopTorrentStream()
        }
    }

    override fun onStreamReady(torrent: Torrent?) {
        debug("onStreamReady $${torrent?.saveLocation?.name}")
    }

    override fun onStreamPrepared(torrent: Torrent?) {
        debug("onStreamPrepared ${torrent?.saveLocation?.name}")
    }

    override fun onStreamStopped() {
        debug("onStreamStopped ")
    }

    override fun onStreamStarted(torrent: Torrent?) {
        debug("onStreamStarted ${torrent?.saveLocation?.name}")
    }

    override fun onStreamProgress(torrent: Torrent?, status: StreamStatus?) {
        debug("onStreamProgress $torrent status ${status?.bufferProgress}")
    }

    override fun onServerReady(url: String?) {
        debug("onServerReady $url")

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.setDataAndType(Uri.parse(url), "video/mp4")
        startActivity(intent)
    }

    override fun onStreamError(torrent: Torrent?, e: Exception?) {
        debug("onStreamError $torrent exception ${e?.message.toString()}")

    }

}