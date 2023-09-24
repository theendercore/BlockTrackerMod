package org.teamvoided.stat_tracker.networking

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import org.teamvoided.stat_tracker.StatTracker
import org.teamvoided.stat_tracker.client.ClientDataTracker.blocksMinedGlobal
import org.teamvoided.stat_tracker.client.ClientDataTracker.blocksMinedSelf

object NetworkManager {

    val BLOCK_COUNT_UPDATE = StatTracker.id("block_count_update")
    fun initClient() {
        ClientPlayNetworking.registerGlobalReceiver(BLOCK_COUNT_UPDATE) { _, _, buf, _ ->
            val pkg = BlockCountUpdateS2C(buf)
            blocksMinedGlobal = pkg.blocksMinedGlobal
            blocksMinedSelf = pkg.blocksMinedSelf
        }
    }
}