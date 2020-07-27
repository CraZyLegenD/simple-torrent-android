package com.masterwok.demosimpletorrentandroid.activities

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.masterwok.demosimpletorrentandroid.R
import com.masterwok.demosimpletorrentandroid.adapters.TabFragmentPagerAdapter
import com.masterwok.demosimpletorrentandroid.fragments.TorrentFragment
import com.masterwok.simpletorrentandroid.downloader.TorrentSessionOptions
import com.masterwok.simpletorrentandroid.downloader.models.TorrentSessionStatus


/**
 * This activity is responsible for creating and presenting multiple torrent fragments.
 * The [@see TorrentSession] and its associated [@see TorrentSessionListener] exist within
 * [@see TorrentFragment]. In the real world, you would want to have the
 * [@see TorrentSession] live in a foreground service so its not interrupted when the device
 * goes to sleep.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var buttonAddTorrent: AppCompatButton

    private val torrentUrls = arrayOf(
            "http://www.frostclick.com/torrents/video/animation/Big_Buck_Bunny_1080p_surround_frostclick.com_frostwire.com.torrent"
            , "magnet:?xt=urn:btih:08ada5a7a6183aae1e09d831df6748d566095a10&dn=Sintel&tr=udp%3A%2F%2Fexplodie.org%3A6969&tr=udp%3A%2F%2Ftracker.coppersurfer.tk%3A6969&tr=udp%3A%2F%2Ftracker.empire-js.us%3A1337&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337&tr=wss%3A%2F%2Ftracker.btorrent.xyz&tr=wss%3A%2F%2Ftracker.fastcast.nz&tr=wss%3A%2F%2Ftracker.openwebtorrent.com&ws=https%3A%2F%2Fwebtorrent.io%2Ftorrents%2F&xs=https%3A%2F%2Fwebtorrent.io%2Ftorrents%2Fsintel.torrent"
    )

    private val torrentSessionOptions get() = TorrentSessionOptions(
            downloadLocation = filesDir
            , onlyDownloadLargestFile = true
            , enableLogging = false
            , shouldStream = true
    )

    private val torrentSessionPagerAdapter = TabFragmentPagerAdapter<TorrentFragment, TorrentSessionStatus>(
            supportFragmentManager
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViewComponents()
        subscribeToViewComponents()
        initTabLayout()
    }

    private fun initTabLayout() {
        viewPager.adapter = torrentSessionPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

    private fun subscribeToViewComponents() {
        buttonAddTorrent.setOnClickListener {
            // startFilePickerActivity() // Be sure to comment the two lines below if you uncomment this.
            val tabFragment = createTabWithUri(Uri.parse(torrentUrls[torrentSessionPagerAdapter.count]))
            torrentSessionPagerAdapter.addTab(tabFragment)

            if (torrentSessionPagerAdapter.count == torrentUrls.size) {
                buttonAddTorrent.apply {
                    text = context.getString(R.string.button_all_torrents_added)
                    isEnabled = false
                }
            }
        }
    }


    private fun createTabWithUri(torrentUri: Uri): TorrentFragment =
            TorrentFragment.newInstance(
                    this
                    , torrentSessionPagerAdapter.count + 1
                    , torrentUri
                    , torrentSessionOptions
            )

    private fun bindViewComponents() {
        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)
        buttonAddTorrent = findViewById(R.id.button_add_torrent)
    }




}
