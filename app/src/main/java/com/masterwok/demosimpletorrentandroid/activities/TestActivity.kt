package com.masterwok.demosimpletorrentandroid.activities

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.crazylegend.kotlinextensions.coroutines.mainCoroutine
import com.crazylegend.kotlinextensions.file.clearFilesDir
import com.frostwire.jlibtorrent.TorrentHandle
import com.masterwok.demosimpletorrentandroid.R
import com.masterwok.simpletorrentandroid.TorrentSession
import com.masterwok.simpletorrentandroid.TorrentSessionOptions
import com.masterwok.simpletorrentandroid.contracts.TorrentSessionListener
import com.masterwok.simpletorrentandroid.models.TorrentSessionStatus
import kotlinx.android.synthetic.main.activity_test.*

class TestActivity : AppCompatActivity(), TorrentSessionListener {


    private val link1 = "magnet:?xt=urn:btih:cc222eaef11ab8c620bbeb9bdb66f6505bd04548&dn=Bruce Springsteen - Western Stars (2019) Mp3 (320 kbps) [Hunter]&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969&tr=udp%3A%2F%2Ftracker.coppersurfer.tk%3A6969&tr=udp%3A%2F%2Feddie4.nl%3A6969&tr=udp%3A%2F%2Ftracker.pirateparty.gr%3A6969&tr=udp%3A%2F%2Fopentrackr.org%3A1337&tr=udp%3A%2F%2Ftracker.zer0day.to%3A1337"
    private val link2 = "magnet:?xt=urn:btih:588059847328f9721f574d50e73f680f160e825b&dn=Drake and Future - What A Time To Be Alive 2 (2020) Mp3 320kbps [PMEDIA] %E2%AD%90%EF%B8%8F&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969&tr=udp%3A%2F%2Ftracker.coppersurfer.tk%3A6969&tr=udp%3A%2F%2Feddie4.nl%3A6969&tr=udp%3A%2F%2Ftracker.pirateparty.gr%3A6969&tr=udp%3A%2F%2Fopentrackr.org%3A1337&tr=udp%3A%2F%2Ftracker.zer0day.to%3A1337"
    private lateinit var torrentSession: TorrentSession

    private val torrentSessionOptions
        get() = TorrentSessionOptions(
                downloadLocation = filesDir
                , onlyDownloadLargestFile = false
                , enableLogging = false
                , shouldStream = true
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        testButton.setOnClickListener {
        }

        filesDir.deleteRecursively()


        torrentSession = TorrentSession(torrentSessionOptions)
        torrentSession.listener = this

        listOf(link1, link2).forEach { magnetUri ->
            mainCoroutine {
                torrentSession.start(this, Uri.parse(magnetUri))
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        torrentSession.listener = null
        torrentSession.stop()
    }


    override fun onPieceFinished(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        /*Log.i("onPieceFinished", torrentHandle.name().toString())
        Log.i("onPieceFinished", torrentSessionStatus.toString())*/
    }

    override fun onAddTorrent(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
         Log.i("onAddTorrent", torrentHandle.name().toString())
         Log.i("onAddTorrent", torrentSessionStatus.toString())

    }

    override fun onTorrentError(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        /* Log.i("onTorrentError", torrentHandle.name().toString())
         Log.i("onTorrentError", torrentSessionStatus.toString())*/
    }

    override fun onTorrentFinished(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        Log.i("onTorrentFinished", torrentHandle.name().toString() + torrentHandle.infoHash().toHex().toString())
        Log.i("onTorrentFinished", torrentSessionStatus.toString())
    }

    override fun onMetadataFailed(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        /*Log.i("onMetadataFailed", torrentHandle.name().toString())
        Log.i("onMetadataFailed", torrentSessionStatus.toString())*/
    }

    override fun onMetadataReceived(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        /*Log.i("onMetadataReceived", torrentHandle.name().toString())
        Log.i("onMetadataReceived", torrentSessionStatus.toString())*/
    }

    override fun onTorrentDeleteFailed(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        /* Log.i("onTorrentDeleteFailed", torrentHandle.name().toString())
         Log.i("onTorrentDeleteFailed", torrentSessionStatus.toString())*/
    }

    override fun onTorrentPaused(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        /* Log.i("onTorrentPaused", torrentHandle.name().toString())
         Log.i("onTorrentPaused", torrentSessionStatus.toString())*/
    }

    override fun onTorrentDeleted(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        /* Log.i("onTorrentDeleted", torrentHandle.name().toString())
         Log.i("onTorrentDeleted", torrentSessionStatus.toString())*/
    }

    override fun onTorrentRemoved(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        /*Log.i("onTorrentRemoved", torrentHandle.name().toString())
        Log.i("onTorrentRemoved", torrentSessionStatus.toString())*/
    }

    override fun onTorrentResumed(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        /*Log.i("onTorrentResumed", torrentHandle.name().toString())
        Log.i("onTorrentResumed", torrentSessionStatus.toString())*/
    }

    override fun onBlockUploaded(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        /*Log.i("onBlockUploaded", torrentHandle.name().toString())
        Log.i("onBlockUploaded", torrentSessionStatus.toString())*/

    }
}
