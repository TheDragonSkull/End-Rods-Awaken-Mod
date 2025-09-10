package net.thedragonskull.rodsawaken.network;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thedragonskull.rodsawaken.block.entity.AwakenedEndRodBE;
import net.thedragonskull.rodsawaken.screen.AwakenedEndRodMenu;

public class ServerPayloadHandler {

    private static final ServerPayloadHandler INSTANCE = new ServerPayloadHandler();

    public static ServerPayloadHandler getInstance() {
        return INSTANCE;
    }

    public void handleToggleBlockedSlot(C2SToggleBlockedSlotPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();

            if (player.containerMenu instanceof AwakenedEndRodMenu menu) {
                AwakenedEndRodBE be = menu.getBlockEntity();

                boolean wasBlocked = be.isSlotBlocked(msg.slot());
                be.toggleBlocked(msg.slot());
                be.setChanged();
                be.syncToClient();

                player.playNotifySound(
                        wasBlocked ? SoundEvents.BAMBOO_WOOD_TRAPDOOR_OPEN : SoundEvents.BAMBOO_WOOD_TRAPDOOR_CLOSE,
                        SoundSource.BLOCKS,
                        1.0f,
                        1.0f
                );
            }
        });
    }

    public void handleClearPotionSlot(C2SClearPotionSlotPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();

            ServerLevel level = player.serverLevel();
            BlockEntity be = level.getBlockEntity(msg.pos());

            if (be instanceof AwakenedEndRodBE awakened) {
                awakened.clearPotionSlot(msg.slot());
                level.playSound(null, msg.pos(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.0F);
            }
        });
    }

}
