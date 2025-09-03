package net.thedragonskull.rodsawaken.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

    private final int[] potionDurations = new int[3]; // Potion total duration
    private final int[] potionTimers = new int[3]; // Time left
    private final int[] potionColors = new int[3]; // Potion Effect Color
    private final List<MobEffectInstance>[] potionEffects = new List[3];

    private final ItemStackHandler items = new ItemStackHandler(4) {

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            if (slot >= 0 && slot <= 2) {
                ItemStack stack = items.getStackInSlot(slot);
                if (!stack.isEmpty() && stack.getItem() == Items.POTION) {
                    consumePotionIntoSlot(slot, stack);
                }
            }
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
        for (int i = 0; i < 3; i++) {
            potionEffects[i] = new ArrayList<>();
        }
    }

    public void tick() {
        if (level != null && !level.isClientSide) {
            boolean changed = false;

            for (int i = 0; i < 3; i++) {
                if (potionTimers[i] > 0) {
                    potionTimers[i]--;

                    if (potionTimers[i] == 0) {
                        potionDurations[i] = 0;
                        potionColors[i] = 0;
                    }

                    changed = true;
                }
            }

            if (changed) {
                setChanged();
            }
        }
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

    public int getPotionColor(int slot) {
        return potionColors[slot];
    }

    public float getPotionProgress(int slot) {
        if (potionDurations[slot] == 0) return 0f;
        return (float)potionTimers[slot] / (float)potionDurations[slot];
    }

    public int getPotionTimeLeft(int slot) {
        return potionTimers[slot];
    }

    public List<MobEffectInstance> getPotionEffects(int slot) {
        return potionEffects[slot];
    }

    public int getCombinedPotionColor() {
        int r = 0, g = 0, b = 0, count = 0;

        for (int i = 0; i < 3; i++) {
            int color = potionColors[i];
            if (color != 0) {
                r += (color >> 16) & 0xFF;
                g += (color >> 8) & 0xFF;
                b += color & 0xFF;
                count++;
            }
        }

        if (count == 0) {
            return 0xFFFFFFFF;
        }

        r /= count;
        g /= count;
        b /= count;

        return (r << 16) | (g << 8) | b;
    }

    private void consumePotionIntoSlot(int slot, ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != Items.POTION) return;

        List<MobEffectInstance> effects = PotionUtils.getMobEffects(stack);
        if (effects.isEmpty()) return;

        potionEffects[slot].clear();
        potionEffects[slot].addAll(effects);

        int maxDuration = effects.stream()
                .mapToInt(MobEffectInstance::getDuration)
                .max()
                .orElse(0);

        potionDurations[slot] = maxDuration;
        potionTimers[slot] = maxDuration;
        potionColors[slot] = PotionUtils.getColor(stack);

        stack.shrink(1);

        if (this.level != null) {
            this.level.playSound(null, this.worldPosition, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS);
            this.level.playSound(null, this.worldPosition, SoundEvents.ENDER_EYE_DEATH, SoundSource.BLOCKS);
        }

        if (stack.isEmpty()) {
            items.setStackInSlot(slot, ItemStack.EMPTY);
        } else {
            items.setStackInSlot(slot, stack);
        }

        setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound("Inventory"));

        int[] d = tag.getIntArray("PotionDurations");
        int[] t = tag.getIntArray("PotionTimers");
        int[] c = tag.getIntArray("PotionColors");

        System.arraycopy(d, 0, potionDurations, 0, Math.min(d.length, potionDurations.length));
        System.arraycopy(t, 0, potionTimers, 0, Math.min(t.length, potionTimers.length));
        System.arraycopy(c, 0, potionColors, 0, Math.min(c.length, potionColors.length));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", items.serializeNBT());

        tag.putIntArray("PotionDurations", potionDurations);
        tag.putIntArray("PotionTimers", potionTimers);
        tag.putIntArray("PotionColors", potionColors);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        if (level != null && level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
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
