package org.teamvoided.stat_tracker.client

import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.text.Text
import org.teamvoided.stat_tracker.config.MockConfig
import org.teamvoided.voidlib.core.ARGB
import org.teamvoided.voidlib.core.datastructures.vector.Vec2f
import org.teamvoided.voidlib.core.datastructures.vector.Vec2i
import org.teamvoided.voidlib.vui.v2.node.*
import org.teamvoided.voidlib.vui.v2.node.layout.BoundingBox
import org.teamvoided.voidlib.vui.v2.screen.VoidUIAdapter
import org.teamvoided.voidlib.vui.v2.screen.VuiScreen

class ConfigScreen : VuiScreen<BoxNode>(Text.literal("config")) {
    override val uiAdapter: VoidUIAdapter<BoxNode> =
        VoidUIAdapter.create(this) { pos, size -> BoxNode(pos, size, ARGB(0, 0, 0, 0)) }
    private val tRend: TextRenderer = MinecraftClient.getInstance().textRenderer

    val container = ContainerNode(Vec2i(0, 0), Vec2i(0, 0), false)
    val background = BoxNode(Vec2i(0, 0), Vec2i(0, 0), ARGB(35, 0, 0, 0))

    val posOutline =
        BoxNode(Vec2i(0, 0), Vec2i(81, tRend.fontHeight * 2 + 6), ARGB(65, 255, 255, 255))
    val movable = MovableNode(posOutline)

    val closeButton = ButtonNode(Vec2i(0, 0), Vec2i(32, 16), Text.literal("Close"))
    private var screenSize = Vec2i(0, 0)

    override fun vuiInit() {
        MinecraftClient.getInstance().window.scaleFactor = oldScaleFactor
        screenSize = Vec2i(client!!.window.scaledWidth, client!!.window.scaledHeight)

        closeButton.buttonPressCallback += { closeScreen() }

        root.addChild(background)
        root.addChild(container)
        container.addChild(movable)
        movable.moveCallback += {
            if (BoundingBox.of(Vec2i(0, 0), screenSize).isTouching(it))
                MockConfig.location = ((it + (posOutline.size / 2)).to2f() / screenSize.to2f())
        }
        root.addChild(closeButton)

        closeButton.globalPos = Vec2f(screenSize.x * .5f - (closeButton.size.x / 2), screenSize.y * .8f).to2i()
        movable.globalPos = (screenSize.to2f() * MockConfig.location).to2i() - (posOutline.size / 2)
    }

    override fun vuiUpdate() {
        MinecraftClient.getInstance().window.scaleFactor = oldScaleFactor
        screenSize = Vec2i(client!!.window.scaledWidth, client!!.window.scaledHeight)

        background.size = screenSize
        container.size = screenSize
    }
}