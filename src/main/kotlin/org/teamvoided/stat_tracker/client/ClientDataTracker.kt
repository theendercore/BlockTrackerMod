package org.teamvoided.stat_tracker.client

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment


@Environment(EnvType.CLIENT)
object ClientDataTracker {
    var blocksMinedGlobal = 0
    var blocksMinedSelf = 0
    fun init (){}
}