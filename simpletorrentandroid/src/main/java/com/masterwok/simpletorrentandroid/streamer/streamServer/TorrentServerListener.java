package com.masterwok.simpletorrentandroid.streamer.streamServer;


import com.masterwok.simpletorrentandroid.streamer.listeners.TorrentListener;

public interface TorrentServerListener extends TorrentListener {

    void onServerReady(String url);

}
