package com.smd.justsleep;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SyncBedStatusPacket implements IMessage {

    private BlockPos pos;

    public SyncBedStatusPacket() {}

    public SyncBedStatusPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        if (buf.readBoolean()) {
            int x = buf.readInt();
            int y = buf.readInt();
            int z = buf.readInt();
            this.pos = new BlockPos(x, y, z);
        } else {
            this.pos = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(pos != null);
        if (pos != null) {
            buf.writeInt(pos.getX());
            buf.writeInt(pos.getY());
            buf.writeInt(pos.getZ());
        }
    }

    public static class Handler implements IMessageHandler<SyncBedStatusPacket, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(SyncBedStatusPacket message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() ->
                    JustSleep.updateClientBedLocation(Minecraft.getMinecraft().player, message.pos)
            );
            return null;
        }
    }
}