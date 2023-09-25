package org.teamvoided.stat_tracker.client

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.GuiGraphics
import org.teamvoided.stat_tracker.StatTracker
import org.teamvoided.stat_tracker.config.MockConfig
import org.teamvoided.voidlib.core.ARGB
import org.teamvoided.voidlib.core.datastructures.vector.Vec2i
import org.teamvoided.voidlib.core.f
import kotlin.math.floor

object HedRenderer {
    private const val BAR_WIDTH = 81
    private val ICONS_TEXTURE = StatTracker.id("textures/gui/icons.png")

    fun init() {
        HudRenderCallback.EVENT.register { c, _ ->
            val goal = MockConfig.goal
            val textRend = MinecraftClient.getInstance().textRenderer
            val number = floor((((100 * ClientDataTracker.blocksMinedGlobal) / goal.f) * 100).toDouble()) / 100f
            val text = "${ClientDataTracker.blocksMinedGlobal}/${goal}"
            val pos = (Vec2i(c.scaledWindowWidth, c.scaledWindowHeight).to2f() * MockConfig.location).to2i()


            c.drawText(textRend, text, pos.x - (textRend.getWidth(text) / 2), pos.y - 11, -1, true)
            c.renerBlockBar(pos, number, goal, textRend)
        }
    }

    private fun GuiGraphics.renerBlockBar(pos: Vec2i, number: Double, limit: Int, r: TextRenderer) {
        var mappedWidth = (((BAR_WIDTH - 1) * number) / 100).toInt()
        if (mappedWidth >= BAR_WIDTH - 4 && ClientDataTracker.blocksMinedGlobal < limit) mappedWidth = BAR_WIDTH - 4
        this.drawTexture(ICONS_TEXTURE, pos.x - (BAR_WIDTH / 2), pos.y, 0, 0, BAR_WIDTH, 7)
        if (mappedWidth > 0) this.drawTexture(ICONS_TEXTURE, pos.x - (BAR_WIDTH / 2), pos.y, 0, 7, mappedWidth, 7)

        val n = "$number%"
        val k: Int = pos.x - r.getWidth(n) / 2
        this.drawText(r, n, k + 1, pos.y, 0, false)
        this.drawText(r, n, k - 1, pos.y, 0, false)
        this.drawText(r, n, k, pos.y + 1, 0, false)
        this.drawText(r, n, k, pos.y - 1, 0, false)
        this.drawText(r, n, k, pos.y, ARGB(75, 249, 105).toInt(), false)
    }

}