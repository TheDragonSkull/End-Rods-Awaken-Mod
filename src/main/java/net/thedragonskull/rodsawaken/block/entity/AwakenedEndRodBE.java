package net.thedragonskull.rodsawaken.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.ItemStackHandler;
import net.thedragonskull.rodsawaken.particle.ModParticles;
import net.thedragonskull.rodsawaken.screen.AwakenedEndRodMenu;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static net.thedragonskull.rodsawaken.block.custom.AwakenedEndRod.LIT;

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
        if (level == null) return;

        if (!level.isClientSide) {
            boolean changed = false;

            for (int i = 0; i < 3; i++) {
                if (potionTimers[i] > 0) {
                    potionTimers[i]--;

                    if (potionTimers[i] == 0) {
                        potionDurations[i] = 0;
                        potionColors[i] = 0;
                        potionEffects[i].clear();
                    }

                    changed = true;
                    if (level != null) {
                        BlockState state = getBlockState();
                        level.sendBlockUpdated(worldPosition, state, state, 3);
                    }
                }
            }

            if (changed) {
                setChanged();
                BlockState state = getBlockState();
                level.sendBlockUpdated(worldPosition, state, state, 3);
            }

            if (level.getBlockState(worldPosition).getValue(LIT)) {
                if (level.getGameTime() % 2 == 0) {

                    boolean anyEffect = false;
                    for (int i = 0; i < 3; i++) {
                        if (hasEffectInSlot(i)) {
                            anyEffect = true;
                            break;
                        }
                    }
                    if (!anyEffect) return;

                    double x = worldPosition.getX() + 0.5;
                    double y = worldPosition.getY() + 0.5;
                    double z = worldPosition.getZ() + 0.5;

                    ((ServerLevel) level).sendParticles(
                            ModParticles.AWAKENED_END_ROD_GLITTER.get(),
                            x, y, z,
                            2,
                            0.1, 0.25, 0.1,
                            0.2
                    );
                }

                applyEffectsToNearby();
            }
        }
    }

    private void applyEffectsToNearby() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        AABB area = new AABB(worldPosition).inflate(5);
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive);

        int refreshInterval = 40; // 2s

        for (LivingEntity target : targets) {
            for (int i = 0; i < 3; i++) {
                if (potionTimers[i] <= 0 || potionEffects[i].isEmpty()) continue;

                for (MobEffectInstance effect : potionEffects[i]) {
                    MobEffect mob = effect.getEffect();
                    if (mob.isInstantenous()) continue;

                    MobEffectInstance current = target.getEffect(mob);
                    boolean shouldApply = false;

                    if (current == null) {
                        shouldApply = true;
                    } else {
                        if (current.getAmplifier() < effect.getAmplifier()) {
                            shouldApply = true;
                        } else if (current.getDuration() <= refreshInterval / 2) {
                            shouldApply = true;
                        }
                    }

                    if (shouldApply) {
                        int applyDuration = Math.min(refreshInterval, potionTimers[i]);
                        MobEffectInstance toApply = new MobEffectInstance(
                                mob,
                                applyDuration,
                                effect.getAmplifier(),
                                effect.isAmbient(),
                                effect.isVisible(),
                                effect.showIcon()
                        );
                        target.addEffect(toApply);
                    }
                }
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

    public boolean hasEffectInSlot(int slot) {
        return potionTimers[slot] > 0 && !potionEffects[slot].isEmpty();
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

    public void clearPotionSlot(int slot) {
        potionEffects[slot].clear();
        potionDurations[slot] = 0;
        potionTimers[slot] = 0;
        potionColors[slot] = 0;

        //items.setStackInSlot(slot, ItemStack.EMPTY);

        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
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
            items.setStackInSlot(slot, new ItemStack(Items.GLASS_BOTTLE));
        } else {
            items.setStackInSlot(slot, stack);
        }

        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", items.serializeNBT());

        tag.putIntArray("PotionDurations", potionDurations);
        tag.putIntArray("PotionTimers", potionTimers);
        tag.putIntArray("PotionColors", potionColors);

        // Save effects
        for (int i = 0; i < potionEffects.length; i++) {
            ListTag listTag = new ListTag();
            for (MobEffectInstance effect : potionEffects[i]) {
                listTag.add(effect.save(new CompoundTag()));
            }
            tag.put("PotionEffects" + i, listTag);
        }
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

        // Load effects
        for (int i = 0; i < potionEffects.length; i++) {
            potionEffects[i].clear();
            ListTag listTag = tag.getList("PotionEffects" + i, Tag.TAG_COMPOUND);
            for (int j = 0; j < listTag.size(); j++) {
                CompoundTag effectTag = listTag.getCompound(j);
                MobEffectInstance effect = MobEffectInstance.load(effectTag);
                if (effect != null) {
                    potionEffects[i].add(effect);
                }
            }
        }
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

    public Container asContainer() {
        SimpleContainer container = new SimpleContainer(items.getSlots());
        for (int i = 0; i < items.getSlots(); i++) {
            container.setItem(i, items.getStackInSlot(i));
        }
        return container;
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
