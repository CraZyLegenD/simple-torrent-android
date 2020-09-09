/*
 *
 *  * This file is part of TorrentStreamer-Android.
 *  *
 *  * TorrentStreamer-Android is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Lesser General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * TorrentStreamer-Android is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  * GNU Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with TorrentStreamer-Android. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.masterwok.simpletorrentandroid.streamer.listeners

import com.frostwire.jlibtorrent.AlertListener
import com.frostwire.jlibtorrent.alerts.Alert
import com.frostwire.jlibtorrent.alerts.AlertType
import com.frostwire.jlibtorrent.alerts.DhtStatsAlert

abstract class DHTStatsAlertListener : AlertListener {
    override fun types(): IntArray {
        return intArrayOf(AlertType.DHT_STATS.swig())
    }

    override fun alert(alert: Alert<*>?) {
        if (alert is DhtStatsAlert) {
            stats(countTotalDHTNodes(alert))
        }
    }

    abstract fun stats(totalDhtNodes: Int)
    private fun countTotalDHTNodes(alert: DhtStatsAlert): Int {
        val routingTable = alert.routingTable()
        var totalNodes = 0
        if (routingTable != null) {
            for (bucket in routingTable) {
                totalNodes += bucket.numNodes()
            }
        }
        return totalNodes
    }
}