package net.thedragonskull.rodsawaken.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.thedragonskull.rodsawaken.RodsAwaken;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RodsAwaken.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int id = 0;

    public static void register() {

        INSTANCE.messageBuilder(ClearPotionSlotPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ClearPotionSlotPacket::encode)
                .decoder(ClearPotionSlotPacket::new)
                .consumerMainThread(ClearPotionSlotPacket::handle)
                .add();

        INSTANCE.messageBuilder(C2SToggleBlockedSlotPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SToggleBlockedSlotPacket::encode)
                .decoder(C2SToggleBlockedSlotPacket::new)
                .consumerMainThread(C2SToggleBlockedSlotPacket::handle)
                .add();

    }

    public static void sendToServer(Object msg) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), msg);
    }

    public static void sendToPlayer(Object msg, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static void sendToAllPlayer(Object msg) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
    }
}
