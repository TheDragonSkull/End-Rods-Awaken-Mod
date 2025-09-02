package net.thedragonskull.rodsawaken.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.thedragonskull.rodsawaken.screen.AwakenedEndRodMenu;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class AwakenedEndRodBE extends BlockEntity implements MenuProvider {

    private final ItemStackHandler items = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (stack.isEmpty()) return false;

            if (slot >= 0 && slot <= 2) {
                return stack.getItem() == Items.POTION;
            }

            if (slot == 3) {
                return stack.getItem() instanceof BlockItem blockItem
                        && blockItem.getBlock() instanceof SculkSensorBlock;
            }

            return false;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    public AwakenedEndRodBE(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.AWAKENED_END_ROD_BE.get(), pPos, pBlockState);
    }


    public void tick() {
    }

    public ItemStack getPotion(int index) {
        if (index >= 0 && index <= 2) {
            return items.getStackInSlot(index);
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getSensor() {
        return items.getStackInSlot(3);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound("Inventory"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", items.serializeNBT());
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    public ItemStackHandler getItems() {
        return items;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Awakened End Rod");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new AwakenedEndRodMenu(id, playerInv, this);
    }
}
