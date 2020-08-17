package com.masterwok.simpletorrentandroid.streamer.streamServer

import com.masterwok.simpletorrentandroid.streamer.StreamStatus
import com.masterwok.simpletorrentandroid.streamer.Torrent
import com.masterwok.simpletorrentandroid.streamer.TorrentOptions
import com.masterwok.simpletorrentandroid.streamer.TorrentStream
import com.masterwok.simpletorrentandroid.streamer.listeners.TorrentListener
import java.io.File
import java.io.IOException
import java.util.*

class TorrentStreamServer (private val serverHost: String, private val serverPort: Int) {
    private val listeners: MutableList<TorrentServerListener> = ArrayList()
    var options: TorrentOptions
        private set
    private var torrentStream: TorrentStream? = null
    private var torrentStreamWebServer: TorrentStreamWebServer? = null
    private val internalListener: TorrentListener = InternalTorrentServerListener()

    fun setTorrentOptions(torrentOptions: TorrentOptions) {
        options = torrentOptions
        if (torrentStream != null) {
            torrentStream?.options = torrentOptions
        }
    }

    val isStreaming: Boolean
        get() = if (torrentStream == null) {
            false
        } else torrentStream?.isStreaming ?: false

    fun resumeSession() {
        if (torrentStream != null) {
            torrentStream?.resumeSession()
        }
    }

    fun pauseSession() {
        if (torrentStream != null) {
            torrentStream?.pauseSession()
        }
    }

    val currentTorrentUrl: String?
        get() = if (torrentStream == null) {
            null
        } else torrentStream?.currentTorrentUrl

    val totalDhtNodes: Int?
        get() = if (torrentStream == null) {
            0
        } else torrentStream?.totalDhtNodes

    val currentTorrent: Torrent?
        get() = if (torrentStream == null) {
            null
        } else torrentStream?.currentTorrent

    val currentStreamUrl: String?
        get() = if (torrentStreamWebServer == null || (torrentStreamWebServer?.wasStarted() == false)) {
            null
        } else torrentStreamWebServer?.streamUrl

    val currentVTTUrl: String?
        get() = if (torrentStreamWebServer == null || (torrentStreamWebServer?.wasStarted() == false)) {
            null
        } else torrentStreamWebServer?.vTTUrl

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

    fun startTorrentStream() {
        torrentStream = TorrentStream.init(options)
        torrentStream?.addListener(internalListener)
    }

    fun stopTorrentStream() {
        torrentStream?.apply {
            if (isStreaming) stopStream()
        }
        torrentStream = null
    }
    /**
     * Start stream download for specified torrent
     *
     * @param torrentUrl [String] .torrent or magnet link
     * @param srtSubtitleFile [File] SRT subtitle
     * @param vttSubtitleFile [File] VTT subtitle
     */
    /**
     * Start stream download for specified torrent
     *
     * @param torrentUrl [String] .torrent or magnet link
     */
    @JvmOverloads
    @Throws(TorrentStreamNotInitializedException::class, IOException::class)
    fun startStream(torrentUrl: String, srtSubtitleFile: File? = null, vttSubtitleFile: File? = null) {
        if (torrentStream == null) {
            throw TorrentStreamNotInitializedException()
        }
        torrentStream?.startStream(torrentUrl)
        torrentStreamWebServer = TorrentStreamWebServer(serverHost, serverPort)
        torrentStreamWebServer?.setSrtSubtitleLocation(srtSubtitleFile)
        torrentStreamWebServer?.setVttSubtitleLocation(vttSubtitleFile)
        torrentStreamWebServer?.start()
    }

    /**
     * Set SRT subtitle file of active stream
     * @param file [File] SRT subtitle
     */
    fun setStreamSrtSubtitle(file: File?) {
        torrentStreamWebServer?.apply {
            if (wasStarted()) setSrtSubtitleLocation(file)
        }
    }

    /**
     * Set SRT subtitle file of active stream
     * @param file [File] VTT subtitle
     */
    fun setStreamVttSubtitle(file: File?) {
        torrentStreamWebServer?.apply {
            if (wasStarted()) setVttSubtitleLocation(file)
        }
    }

    /**
     * Stop current torrent stream
     */
    fun stopStream() {
        torrentStreamWebServer?.apply {
            if (wasStarted()) stop()
        }
        torrentStream?.apply {
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
            torrentStreamWebServer?.setVideoTorrent(torrent)
            torrentStreamWebServer?.streamUrl?.let { onServerReady(it) }
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

    init {
        options = TorrentOptions.Builder().build()
    }
}