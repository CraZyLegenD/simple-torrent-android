package com.masterwok.demosimpletorrentandroid.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.crazylegend.kotlinextensions.coroutines.defaultCoroutine
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
import java.io.File
import java.io.IOException
import java.net.UnknownHostException


/**
 * Created by crazy on 8/9/20 to long live and prosper !
 */
class StreamActivity : AppCompatActivity(R.layout.activity_test), TorrentServerListener {

    private val vttFile get() = File(filesDir, "test.vtt")
    private val link1 = "magnet:?xt=urn:btih:fa8bcc16683b3c4bf2178cd2e965f06ee23b6f36&dn=The.Food.Guide.to.Love.2013.BRRip.x264-HUD&tr=http%3a%2f%2ftracker.zamunda.net%2fannounce.php%3fpasskey%3d994c30f57886bff77851922c2f0a343e"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resources.openRawResource(R.raw.sample).use {
            vttFile.writeBytes(it.readBytes())
        }

        debug("VTT FILE ${vttFile.readText()}")
        debug("VTT FILE ${vttFile.path}")

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

        val torrentStreamServer = TorrentStreamServer(ipAddress, 8080)
        torrentStreamServer.setTorrentOptions(torrentOptions)
        torrentStreamServer.startTorrentStream()
        torrentStreamServer.addListener(this)


        try {
            torrentStreamServer.startStream(link1)
            torrentStreamServer.setStreamVttSubtitle(vttFile)
            debug("VTT LINK ${torrentStreamServer.currentVTTUrl}")
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show()
        } catch (e: TorrentStreamNotInitializedException) {
            e.printStackTrace()
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.testButton).setOnClickListenerCooldown {
            torrentStreamServer.stopTorrentStream()
            torrentStreamServer.stopStream()
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
        debug("onStreamProgress ${torrent?.videoFile?.name} status ${status?.bufferProgress} ${status?.progress}")
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