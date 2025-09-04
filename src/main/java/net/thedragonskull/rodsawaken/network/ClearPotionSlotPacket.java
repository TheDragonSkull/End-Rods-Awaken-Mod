package net.thedragonskull.rodsawaken.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.thedragonskull.rodsawaken.block.entity.AwakenedEndRodBE;

import java.util.function.Supplier;

public class ClearPotionSlotPacket {
    private final int slot;
    private final BlockPos pos;

    public ClearPotionSlotPacket(int slot, BlockPos pos) {
        this.slot = slot;
        this.pos = pos;
    }

    public ClearPotionSlotPacket(FriendlyByteBuf buf) {
        this.slot = buf.readInt();
        this.pos = buf.readBlockPos();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(slot);
        buf.writeBlockPos(pos);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ServerLevel level = player.serverLevel();
            BlockEntity be = level.getBlockEntity(pos);

            if (be instanceof AwakenedEndRodBE awakened) {
                awakened.clearPotionSlot(slot);
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
