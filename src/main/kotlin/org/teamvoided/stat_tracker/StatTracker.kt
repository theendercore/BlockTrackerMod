package org.teamvoided.stat_tracker

import com.mojang.blaze3d.platform.InputUtil
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.networking.v1.*
import net.minecraft.client.option.KeyBind
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.tag.BlockTags
import net.minecraft.scoreboard.ScoreboardCriterion
import net.minecraft.scoreboard.ScoreboardCriterion.RenderType.INTEGER
import net.minecraft.scoreboard.ScoreboardObjective
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.world.World
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.teamvoided.stat_tracker.networking.NetworkManager.BLOCK_COUNT_UPDATE
import org.teamvoided.stat_tracker.client.ClientDataTracker
import org.teamvoided.stat_tracker.client.Hud
import org.teamvoided.stat_tracker.networking.BlockCountUpdateS2C
import org.teamvoided.stat_tracker.networking.NetworkManager

@Suppress("unused")
object StatTracker {

    const val MODID: String = "stat_tracker"
    fun id(path: String) = Identifier(MODID, path)


    @JvmField
    val LOGGER: Logger = LoggerFactory.getLogger(StatTracker::class.java)

    private val testKey: KeyBind = KeyBindingHelper.registerKeyBinding(KeyBind("test", InputUtil.KEY_R_CODE, ""))

    fun commonInit() {
        LOGGER.info("Hello from Common")

        ServerPlayConnectionEvents.JOIN.register { h, _, _ ->
            ServerPlayNetworking.send(
                h.player, BLOCK_COUNT_UPDATE,
                BlockCountUpdateS2C(
                    getTotalBlockCount(h.player.world),
                    getPlayerBlockCount(h.player.world, h.player)
                ).write()
            )
        }

        PlayerBlockBreakEvents.AFTER.register { world, player, _, state, _ ->
            if (!world.isClient) {
                if (!state.isIn(BlockTags.FLOWERS)) {
                    val obj = getScores(world)
                    world.scoreboard.getPlayerScore(player.entityName, obj).score += 1

                    val totalCount = getTotalBlockCount(world)
                    PlayerLookup.all(world.server).forEach {
                        ServerPlayNetworking.send(
                            it, BLOCK_COUNT_UPDATE,
                            BlockCountUpdateS2C(totalCount, getPlayerBlockCount(world, it)).write()
                        )
                    }

                }
            }
        }
    }

    fun clientInit() {
        LOGGER.info("Hello from Client")
        ClientDataTracker.init()
        NetworkManager.initClient()
        Hud.init()
        ClientTickEvents.END_CLIENT_TICK.register { if (testKey.wasPressed()) LOGGER.info("heyo") }
    }


    private const val scoreKey: String = "blocks_mined"
    private fun getScores(world: World): ScoreboardObjective =
        if (world.scoreboard.getNullableObjective(scoreKey) != null) world.scoreboard.getObjective(scoreKey)
        else world.scoreboard.addObjective(scoreKey, ScoreboardCriterion.DUMMY, Text.literal("Blocks Mined"), INTEGER)


    private fun getTotalBlockCount(world: World): Int {
        val allScores = world.scoreboard.getAllPlayerScores(getScores(world)).map { it.score }
        return if (allScores.isEmpty()) 0 else allScores.reduce { i, j -> i + j }
    }

    private fun getPlayerBlockCount(world: World, player: PlayerEntity): Int =
        world.scoreboard.getPlayerScore(player.entityName, getScores(world)).score

}