package org.teamvoided.template

import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.tag.BlockTags
import net.minecraft.scoreboard.ScoreboardCriterion
import net.minecraft.scoreboard.ScoreboardCriterion.RenderType.*
import net.minecraft.scoreboard.ScoreboardObjective
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.world.World
import org.slf4j.LoggerFactory
import kotlin.math.floor

@Suppress("unused")
object Template {

    const val MODID: String = "template"
    const val scoreKey: String = "mine_count"

    val BLOCK_COUNT_UPDATE = id("block_count_update")
    private var blockCount = 0
    private val goal = 1_000_000


    @JvmField
    val LOGGER = LoggerFactory.getLogger(Template::class.java)

    fun commonInit() {
        LOGGER.info("Hello from Common")
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            val world = handler.player.world

            val obj = getOrCreateScore(world)

            val buf = PacketByteBuf(Unpooled.buffer())
            buf.writeInt(getBlockCount(obj, world))

            ServerPlayNetworking.send(handler.player, BLOCK_COUNT_UPDATE, buf)
        }


        PlayerBlockBreakEvents.AFTER.register { world, player, _, state, _ ->
            if (!world.isClient) {
                if (!state.isIn(BlockTags.FLOWERS)) {
                    val obj = getOrCreateScore(world)

                    val playerScores = world.scoreboard.getPlayerScore(player.entityName, obj)
                    playerScores.score += 1

                    val buf = PacketByteBuf(Unpooled.buffer())
                    buf.writeInt(getBlockCount(obj, world))
                    PlayerLookup.all(world.server).stream().forEach {
                        ServerPlayNetworking.send(it, BLOCK_COUNT_UPDATE, buf)
                    }

                }
            }
        }
    }

    fun clientInit() {
        LOGGER.info("Hello from Client")
        ClientPlayNetworking.registerGlobalReceiver(BLOCK_COUNT_UPDATE) { _, _, buf, _ -> blockCount = buf.readInt() }

        HudRenderCallback.EVENT.register { c, _ ->
            val client = MinecraftClient.getInstance()
            val textRend = client.textRenderer
            val number = floor((((100 * blockCount) / goal.toFloat())*100).toDouble())/100f
            val text =
                "Blocks Mined : $blockCount/$goal (${number}%)"

            val z = (textRend.getWidth(text) / 2)

            c.drawText(
                textRend,
                text,
                c.scaledWindowWidth / 2 - z,
                c.scaledWindowHeight / 5,
                0xffffff,
                true
            )
        }
    }

    private fun getOrCreateScore(world: World): ScoreboardObjective {
        val score = world.scoreboard
        return if (score.getNullableObjective(scoreKey) != null) score.getObjective(scoreKey)
        else score.addObjective(scoreKey, ScoreboardCriterion.DUMMY, Text.literal("Blocks Mined"), INTEGER)
    }

    private fun getBlockCount(obj: ScoreboardObjective, world: World): Int {
        val allScores = world.scoreboard.getAllPlayerScores(obj)
        var sum = 0
        for (score in allScores) sum += score.score
        return sum
    }

    fun id(path: String) = Identifier(MODID, path)
    fun mc(path: String) = Identifier(path)
}