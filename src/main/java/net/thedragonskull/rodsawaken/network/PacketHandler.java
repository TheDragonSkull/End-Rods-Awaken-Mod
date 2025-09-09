package net.thedragonskull.rodsawaken.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;
import net.thedragonskull.rodsawaken.RodsAwaken;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = ChannelBuilder.named(
            ResourceLocation.fromNamespaceAndPath(RodsAwaken.MOD_ID, "main"))
            .serverAcceptedVersions((status, version) -> true)
            .clientAcceptedVersions((status, version) -> true)
            .networkProtocolVersion(1)
            .simpleChannel();

    private static int id = 0;

    public static void register() {

        INSTANCE.messageBuilder(C2SClearPotionSlotPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SClearPotionSlotPacket::encode)
                .decoder(C2SClearPotionSlotPacket::new)
                .consumerMainThread(C2SClearPotionSlotPacket::handle)
                .add();

        INSTANCE.messageBuilder(C2SToggleBlockedSlotPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SToggleBlockedSlotPacket::encode)
                .decoder(C2SToggleBlockedSlotPacket::new)
                .consumerMainThread(C2SToggleBlockedSlotPacket::handle)
                .add();

    }

    public static void sendToServer(Object msg) {
        INSTANCE.send(msg, PacketDistributor.SERVER.noArg());
    }

    public static void sendToPlayer(Object msg, ServerPlayer player) {
        INSTANCE.send(msg, PacketDistributor.PLAYER.with(player));
    }

    public static void sendToAllPlayer(Object msg) {
        INSTANCE.send(msg, PacketDistributor.ALL.noArg());
    }
}
