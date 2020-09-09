/*
 * Copyright (C) 2015-2018 Sébastiaan (github.com/se-bastiaan)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.masterwok.simpletorrentandroid.streamer.listeners

import com.frostwire.jlibtorrent.AlertListener
import com.frostwire.jlibtorrent.alerts.AddTorrentAlert
import com.frostwire.jlibtorrent.alerts.Alert
import com.frostwire.jlibtorrent.alerts.AlertType

abstract class TorrentAddedAlertListener : AlertListener {
    override fun types(): IntArray {
        return intArrayOf(AlertType.ADD_TORRENT.swig())
    }

    override fun alert(alert: Alert<*>) {
        when (alert.type()) {
            AlertType.ADD_TORRENT -> torrentAdded(alert as AddTorrentAlert)
            else -> {
            }
        }
    }

    abstract fun torrentAdded(alert: AddTorrentAlert?)
}