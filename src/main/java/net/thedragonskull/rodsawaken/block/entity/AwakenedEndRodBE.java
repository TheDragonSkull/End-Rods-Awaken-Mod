package net.thedragonskull.rodsawaken.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.thedragonskull.rodsawaken.screen.AwakenedEndRodMenu;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AwakenedEndRodBE extends BlockEntity implements MenuProvider {

    private final ItemStackHandler items = new ItemStackHandler(4) {

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (stack.isEmpty()) return false;

            if (slot >= 0 && slot <= 2) {
                if (stack.getItem() == Items.POTION) {
                    List<MobEffectInstance> effects = PotionUtils.getMobEffects(stack);

                    for (MobEffectInstance effect : effects) {
                        if (effect.getEffect().isInstantenous()) {
                            return false;
                        }
                    }
                    return true;
                }
                return false;
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

    public int getCombinedPotionColor() {
        List<ItemStack> potions = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ItemStack stack = this.items.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == Items.POTION) {
                potions.add(stack);
            }
        }

        if (potions.isEmpty()) {
            return 0xFFFFFFFF;
        }

        // Mix Potion Effect Colors
        int r = 0, g = 0, b = 0, count = 0;
        for (ItemStack potion : potions) {
            int color = PotionUtils.getColor(potion);
            r += (color >> 16) & 0xFF;
            g += (color >> 8) & 0xFF;
            b += color & 0xFF;
            count++;
        }

        r /= count;
        g /= count;
        b /= count;

        return (r << 16) | (g << 8) | b;
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
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.load(tag);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            this.level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
        }
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
