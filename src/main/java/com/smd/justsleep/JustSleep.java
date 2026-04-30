package com.smd.justsleep;

import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.Mod.EventHandler;
import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class JustSleep {

    static Set<String> playerSpawnSetSkip = new HashSet<>();
    private static BlockPos clientBedPos = null;

    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        NETWORK.registerMessage(SyncBedStatusPacket.Handler.class, SyncBedStatusPacket.class, 0, Side.CLIENT);
        NETWORK.registerMessage(SetPlayerSpawnPacket.Handler.class, SetPlayerSpawnPacket.class, 1, Side.SERVER);
    }

    @SubscribeEvent
    public static void spawnSet(PlayerSetSpawnEvent event) {
        String uuid = event.getEntityPlayer().getUniqueID().toString();
        if (playerSpawnSetSkip.contains(uuid)) {
            playerSpawnSetSkip.remove(uuid);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void wakeUp(PlayerWakeUpEvent event) {
        if (event.shouldSetSpawn()) {
            String uuid = event.getEntityPlayer().getUniqueID().toString();
            if (hasValidBedLocation(event.getEntityPlayer())) {
                playerSpawnSetSkip.add(uuid);
            }
        }
    }

    @SubscribeEvent
    public static void sleep(PlayerSleepInBedEvent event) {
        if (!event.getEntityPlayer().world.isRemote && event.getEntityPlayer() instanceof EntityPlayerMP) {
            updateBedMap((EntityPlayerMP) event.getEntityPlayer());
        }
    }

    public static boolean hasValidBedLocation(EntityPlayer player) {
        return getBedLocation(player) != null;
    }

    public static BlockPos getBedLocation(EntityPlayer player) {
        if (player.world.isRemote) {
            return clientBedPos;
        }
        BlockPos bedLocation = player.getBedLocation(player.dimension);
        if (bedLocation == null) {
            return null;
        }
        return EntityPlayer.getBedSpawnLocation(player.world, bedLocation, false);
    }

    public static void updateClientBedLocation(EntityPlayer player, BlockPos pos) {
        Validate.isTrue(player.world.isRemote);
        clientBedPos = pos;
    }

    public static void updateBedMap(EntityPlayerMP player) {
        BlockPos pos = player.getBedLocation(player.dimension);
        if (!hasValidBedLocation(player)) {
            pos = null;
        }
        NETWORK.sendTo(new SyncBedStatusPacket(pos), player);
    }
}
