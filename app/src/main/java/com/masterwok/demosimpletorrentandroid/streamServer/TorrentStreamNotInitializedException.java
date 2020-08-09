package com.masterwok.demosimpletorrentandroid.streamServer;

public class TorrentStreamNotInitializedException extends Exception {

    public TorrentStreamNotInitializedException() {
        super("TorrentStream has not been initialized yet. Please start TorrentStream before starting a stream.");
    }
}
