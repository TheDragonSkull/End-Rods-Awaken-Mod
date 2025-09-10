package net.thedragonskull.rodsawaken.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.thedragonskull.rodsawaken.RodsAwaken;

public record C2SToggleBlockedSlotPacket(int slot) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<C2SToggleBlockedSlotPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RodsAwaken.MOD_ID, "toggle_blocked_slot_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SToggleBlockedSlotPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    C2SToggleBlockedSlotPacket::slot,
                    C2SToggleBlockedSlotPacket::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
