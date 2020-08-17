package com.masterwok.simpletorrentandroid.streamer.streamServer

import com.masterwok.simpletorrentandroid.streamer.StreamStatus
import com.masterwok.simpletorrentandroid.streamer.Torrent
import com.masterwok.simpletorrentandroid.streamer.TorrentOptions
import com.masterwok.simpletorrentandroid.streamer.TorrentStream
import com.masterwok.simpletorrentandroid.streamer.listeners.TorrentListener
import java.io.File
import java.io.IOException
import java.util.*

class TorrentStreamServer(serverHost: String, serverPort: Int, torrentOptions: TorrentOptions) {
    private val listeners: MutableList<TorrentServerListener> = ArrayList()
    private val torrentStream: TorrentStream = TorrentStream(torrentOptions)
    private val torrentStreamWebServer: TorrentStreamWebServer = TorrentStreamWebServer(serverHost, serverPort)

    val isStreaming: Boolean
        get() = torrentStream.isStreaming

    fun resumeSession() {
        torrentStream.resumeSession()
    }

    fun pauseSession() {
        torrentStream.pauseSession()
    }

    val currentTorrentUrl: String?
        get() = torrentStream.currentTorrentUrl

    val totalDhtNodes: Int?
        get() = torrentStream.totalDhtNodes

    val currentTorrent: Torrent?
        get() = torrentStream.currentTorrent

    val currentStreamUrl: String?
        get() = if (!torrentStreamWebServer.wasStarted()) {
            null
        } else torrentStreamWebServer.streamUrl

    val currentVTTUrl: String?
        get() = if (!torrentStreamWebServer.wasStarted()) {
            null
        } else torrentStreamWebServer.vTTUrl

    fun addListener(listener: TorrentServerListener?) {
        if (listener != null) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: TorrentServerListener?) {
        if (listener != null) {
            listeners.remove(listener)
        }
    }

    fun stopTorrentStream() {
        torrentStream.apply {
            if (isStreaming) stopStream()
        }
        torrentStream.stopStream()
    }

    /**
     * Start stream download for specified torrent
     *
     * @param torrentUrl [String] .torrent or magnet link
     */
    /**
     * Start stream download for specified torrent
     *
     * @param torrentUrl [String] .torrent or magnet link
     */
    fun startStream(torrentUrl: String) {
        torrentStream.startStream(torrentUrl)
        torrentStreamWebServer.start()
    }

    /**
     * Set SRT subtitle file of active stream
     * @param file [File] SRT subtitle
     */
    fun setStreamSrtSubtitle(file: File?) {
        torrentStreamWebServer.apply {
            if (wasStarted()) setSrtSubtitleLocation(file)
        }
    }

    /**
     * Set SRT subtitle file of active stream
     * @param file [File] VTT subtitle
     */
    fun setStreamVttSubtitle(file: File?) {
        torrentStreamWebServer.apply {
            if (wasStarted()) setVttSubtitleLocation(file)
        }
    }

    /**
     * Stop current torrent stream
     */
    fun stopStream() {
        torrentStreamWebServer.apply {
            if (wasStarted()) stop()
        }
        torrentStream.apply {
            if (isStreaming) stopStream()
        }
    }

    private inner class InternalTorrentServerListener : TorrentServerListener {
        override fun onServerReady(url: String) {
            for (listener in listeners) {
                listener.onServerReady(url)
            }
        }

        override fun onStreamPrepared(torrent: Torrent) {
            for (listener in listeners) {
                listener.onStreamPrepared(torrent)
            }
        }

        override fun onStreamStarted(torrent: Torrent) {
            for (listener in listeners) {
                listener.onStreamStarted(torrent)
            }
        }

        override fun onStreamError(torrent: Torrent, e: Exception) {
            for (listener in listeners) {
                listener.onStreamError(torrent, e)
            }
        }

        override fun onStreamReady(torrent: Torrent) {
            for (listener in listeners) {
                listener.onStreamReady(torrent)
            }
            torrentStreamWebServer.setVideoTorrent(torrent)
            onServerReady(torrentStreamWebServer.streamUrl)
        }

        override fun onStreamProgress(torrent: Torrent, streamStatus: StreamStatus) {
            for (listener in listeners) {
                listener.onStreamProgress(torrent, streamStatus)
            }
        }

        override fun onStreamStopped() {
            for (listener in listeners) {
                listener.onStreamStopped()
            }
        }
    }

}