package com.masterwok.simpletorrentandroid.streamer.streamServer

class TorrentStreamNotInitializedException : Exception("TorrentStream has not been initialized yet. Please start TorrentStream before starting a stream.")