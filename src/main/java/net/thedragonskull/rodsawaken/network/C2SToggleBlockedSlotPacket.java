package net.thedragonskull.rodsawaken.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.thedragonskull.rodsawaken.block.entity.AwakenedEndRodBE;
import net.thedragonskull.rodsawaken.screen.AwakenedEndRodMenu;

import java.util.function.Supplier;

public class C2SToggleBlockedSlotPacket {
    private final int slot;

    public C2SToggleBlockedSlotPacket(int slot) {
        this.slot = slot;
    }

    public C2SToggleBlockedSlotPacket(FriendlyByteBuf buf) {
        this.slot = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(slot);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.containerMenu instanceof AwakenedEndRodMenu menu) {
                AwakenedEndRodBE be = menu.getBlockEntity();
                be.toggleBlocked(slot);
                be.setChanged();
                be.syncToClient();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
