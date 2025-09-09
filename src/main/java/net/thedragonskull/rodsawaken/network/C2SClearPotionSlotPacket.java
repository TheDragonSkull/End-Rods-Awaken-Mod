package net.thedragonskull.rodsawaken.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.thedragonskull.rodsawaken.block.entity.AwakenedEndRodBE;

public class C2SClearPotionSlotPacket {
    private final int slot;
    private final BlockPos pos;

    public C2SClearPotionSlotPacket(int slot, BlockPos pos) {
        this.slot = slot;
        this.pos = pos;
    }

    public C2SClearPotionSlotPacket(FriendlyByteBuf buf) {
        this.slot = buf.readInt();
        this.pos = buf.readBlockPos();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(slot);
        buf.writeBlockPos(pos);
    }

    public static void handle(C2SClearPotionSlotPacket msg, CustomPayloadEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player == null) return;

        ServerLevel level = player.serverLevel();
        BlockEntity be = level.getBlockEntity(msg.pos);

        if (be instanceof AwakenedEndRodBE awakened) {
            awakened.clearPotionSlot(msg.slot);
            level.playSound(null, msg.pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.0F);
        }

        ctx.setPacketHandled(true);
    }
}
