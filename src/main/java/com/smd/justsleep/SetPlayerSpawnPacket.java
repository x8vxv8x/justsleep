package com.smd.justsleep;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SetPlayerSpawnPacket implements IMessage {

    public SetPlayerSpawnPacket() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<SetPlayerSpawnPacket, IMessage> {
        @Override
        public IMessage onMessage(SetPlayerSpawnPacket message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            if (player.isPlayerSleeping()) {
                player.setSpawnPoint(player.bedLocation, false);
            }
            return null;
        }
    }
}