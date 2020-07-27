package com.masterwok.demosimpletorrentandroid.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.masterwok.demosimpletorrentandroid.R
import com.masterwok.demosimpletorrentandroid.adapters.TabFragmentPagerAdapter
import com.masterwok.demosimpletorrentandroid.adapters.TorrentPieceAdapter
import com.masterwok.simpletorrentandroid.downloader.models.TorrentSessionStatus


class TorrentPiecesFragment : Fragment(), TabFragmentPagerAdapter.TabFragment<TorrentSessionStatus> {

    private lateinit var recyclerView: RecyclerView

    private val torrentPieceAdapter: TorrentPieceAdapter = TorrentPieceAdapter()

    override fun onCreateView(
            inflater: LayoutInflater
            , container: ViewGroup?
            , savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
                R.layout.fragment_torrent_pieces
                , container
                , false
        )

        bindViewComponents(view)
        initPiecesRecyclerView(view)

        return view
    }

    private fun bindViewComponents(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view_pieces)
    }

    private fun initPiecesRecyclerView(view: View) {
        recyclerView.apply {
            layoutManager = GridLayoutManager(view.context, 16)
            adapter = torrentPieceAdapter
        }
    }

    override fun getTitle(): String = "Pieces"

    override fun configure(model: TorrentSessionStatus) {
        recyclerView.post {
            torrentPieceAdapter.configure(model)
        }
    }
}
