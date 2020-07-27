package com.masterwok.simpletorrentandroid.downloader.models

import android.net.Uri
import com.frostwire.jlibtorrent.TorrentHandle
import com.frostwire.jlibtorrent.TorrentStatus
import com.masterwok.simpletorrentandroid.downloader.extensions.*


/**
 * This class represents the current state of a torrent session. To receive
 * state updates, set the listener of the [@see TorrentSession].
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class TorrentSessionStatus private constructor(
        val magnetUri: Uri
        , val state: TorrentStatus.State
        , val bencode: ByteArray
        , val seederCount: Int
        , val downloadRate: Int
        , val uploadRate: Int
        , val progress: Float
        , val bytesDownloaded: Long
        , val bytesWanted: Long
        , val saveLocationUri: Uri
        , val videoFileUri: Uri
        , val torrentSessionBuffer: TorrentSessionBuffer
) {
    internal companion object {
        fun createInstance(
                magnetUri: Uri
                , torrentHandle: TorrentHandle
                , bencode: ByteArray
                , torrentSessionBuffer: TorrentSessionBuffer
                , saveLocationUri: Uri
                , largestFileUri: Uri
        ): TorrentSessionStatus = TorrentSessionStatus(
                magnetUri
                , torrentHandle.status().state()
                , bencode
                , torrentHandle.getSeederCount()
                , torrentHandle.getDownloadRate()
                , torrentHandle.getUploadRate()
                , torrentHandle.getProgress()
                , torrentHandle.getTotalDone()
                , torrentHandle.getTotalWanted()
                , saveLocationUri
                , largestFileUri
                , torrentSessionBuffer
        )
    }

    val getProgressInPercentage get() =  progress * 100

    override fun toString(): String = "State: $state" +
            ", Progress percentage: $getProgressInPercentage" +
            ", Bencode Size: ${bencode.size}" +
            ", Seeder Count: $seederCount" +
            ", Download Rate: $downloadRate" +
            ", Upload Rate: $uploadRate" +
            ", Progress: $bytesDownloaded/$bytesWanted ($progress)" +
            ", $torrentSessionBuffer" +
            ", Magnet Uri: $magnetUri" +
            ", Save Location: $saveLocationUri" +
            ", Video File: $videoFileUri"
}

