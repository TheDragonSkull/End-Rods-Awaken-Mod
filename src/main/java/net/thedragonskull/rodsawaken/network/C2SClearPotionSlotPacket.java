package net.thedragonskull.rodsawaken.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.thedragonskull.rodsawaken.RodsAwaken;

public record C2SClearPotionSlotPacket(int slot, BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<C2SClearPotionSlotPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RodsAwaken.MOD_ID, "clear_potion_slot_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SClearPotionSlotPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    C2SClearPotionSlotPacket::slot,
                    BlockPos.STREAM_CODEC,
                    C2SClearPotionSlotPacket::pos,
                    C2SClearPotionSlotPacket::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
