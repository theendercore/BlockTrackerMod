package org.teamvoided.stat_tracker.networking

import io.netty.buffer.Unpooled
import net.minecraft.network.PacketByteBuf

data class BlockCountUpdateS2C(val blocksMinedGlobal: Int, val blocksMinedSelf: Int) {
    constructor(buf: PacketByteBuf) : this(buf.readInt(), buf.readInt())
    fun write(): PacketByteBuf {
        val buf = PacketByteBuf(Unpooled.buffer())
        buf.writeInt(blocksMinedGlobal)
        buf.writeInt(blocksMinedSelf)
        return buf
    }

}
