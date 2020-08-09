package com.masterwok.demosimpletorrentandroid.streamServer;


import com.masterwok.simpletorrentandroid.streamer.listeners.TorrentListener;

public interface TorrentServerListener extends TorrentListener {

    void onServerReady(String url);

}
