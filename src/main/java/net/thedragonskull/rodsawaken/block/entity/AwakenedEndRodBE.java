package net.thedragonskull.rodsawaken.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.thedragonskull.rodsawaken.RodsAwaken;
import net.thedragonskull.rodsawaken.particle.ModParticles;
import net.thedragonskull.rodsawaken.screen.AwakenedEndRodMenu;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.thedragonskull.rodsawaken.block.custom.AwakenedEndRod.LIT;

public class AwakenedEndRodBE extends BlockEntity implements MenuProvider {

    private boolean autoMode = false;
    private boolean manualOverride = false;
    private boolean manualOverrideTriggerPlayerNearby = false;
    private boolean forcedLitState = false;

    private final int[] potionDurations = new int[3]; // Potion total duration
    private final int[] potionTimers = new int[3]; // Time left
    private final int[] potionColors = new int[3]; // Potion Effect Color
    private final List<MobEffectInstance>[] potionEffects = new List[3];

    private final boolean[] blockedSlots = new boolean[3];

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

                if (!potionEffects[slot].isEmpty() || isSlotBlocked(slot)) {
                    return false;
                }

                if (stack.getItem() == Items.POTION) {
                    PotionContents potioncontents = stack.get(DataComponents.POTION_CONTENTS);

                    if (potioncontents != null) {
                        for (MobEffectInstance effect : potioncontents.getAllEffects()) {
                            if (effect.getEffect().value().isInstantenous()) {
                                return false;
                            }
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

    public final IItemHandler hopperItemHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return 3;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return items.getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return items.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return items.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return items.isItemValid(slot, stack);
        }
    };


    public AwakenedEndRodBE(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.AWAKENED_END_ROD_BE.get(), pPos, pBlockState);
        for (int i = 0; i < 3; i++) {
            potionEffects[i] = new ArrayList<>();
        }
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        handleSculkAutoMode();

        boolean changed = false;
        boolean lit = getBlockState().getValue(LIT);

        if (lit) {
            for (int i = 0; i < 3; i++) {
                if (potionTimers[i] > 0) {
                    potionTimers[i]--;

                    if (potionTimers[i] == 0) {
                        potionDurations[i] = 0;
                        potionColors[i] = 0;
                        potionEffects[i].clear();

                        level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.0F);
                    }

                    changed = true;
                    if (level != null) {
                        BlockState state = getBlockState();
                        level.sendBlockUpdated(worldPosition, state, state, 3);
                    }
                }
            }
        }

        if (changed) {
            setChanged();
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }

        if (lit) {
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

            if (level.getGameTime() % 80L == 0L) {
                level.playSound(null, worldPosition, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            applyEffectsToNearby();
        }
    }

    private void applyEffectsToNearby() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        AABB area = new AABB(worldPosition).inflate(5);
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive);

        int refreshInterval = 40; // 2s

        Map<Holder<MobEffect>, Integer> mergedLevels = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            if (potionTimers[i] <= 0 || potionEffects[i].isEmpty()) continue;

            for (MobEffectInstance effect : potionEffects[i]) {
                Holder<MobEffect> mob = effect.getEffect();
                if (mob.value().isInstantenous()) continue;

                int levelValue = effect.getAmplifier() + 1;
                mergedLevels.merge(mob, levelValue, Integer::sum);
            }
        }

        mergedLevels.replaceAll((mob, lvl) -> Math.min(lvl, 6));

        for (LivingEntity target : targets) {
            for (Map.Entry<Holder<MobEffect>, Integer> entry : mergedLevels.entrySet()) {
                Holder<MobEffect> mob = entry.getKey();
                int levelValue = entry.getValue();

                int amplifier = levelValue - 1;

                MobEffectInstance current = target.getEffect(mob);
                boolean shouldApply = false;

                if (current == null) {
                    shouldApply = true;
                } else {
                    if (current.getAmplifier() < amplifier) {
                        shouldApply = true;
                    } else if (current.getDuration() <= refreshInterval / 2) {
                        shouldApply = true;
                    }
                }

                if (shouldApply) {
                    int applyDuration = refreshInterval;
                    MobEffectInstance toApply = new MobEffectInstance(
                            mob,
                            applyDuration,
                            amplifier,
                            false,
                            true,
                            true
                    );

                    target.addEffect(toApply);
                }
            }
        }
    }

    private void handleSculkAutoMode() {
        if (level == null || level.isClientSide) return;

        boolean hasNormal = hasNormalSculk();
        boolean hasCalibrated = hasCalibratedSculk();

        if (!hasNormal && !hasCalibrated) {
            autoMode = false;
            manualOverride = false;
            return;
        }

        autoMode = true;

        AABB area = new AABB(worldPosition).inflate(4);

        boolean entityNearby;
        if (hasCalibrated) {
            entityNearby = !level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive).isEmpty();
        } else { // normal sculk
            entityNearby = !level.getEntitiesOfClass(Player.class, area).isEmpty();
        }

        BlockState state = getBlockState();
        boolean lit = state.getValue(LIT);

        if (manualOverride) {
            if (state.getValue(LIT) != forcedLitState) {
                level.setBlock(worldPosition, state.setValue(LIT, forcedLitState), 3);
            }

            if (entityNearby != manualOverrideTriggerPlayerNearby) {
                manualOverride = false;
            }
            return;
        }

        if (entityNearby && !lit) {
            level.setBlock(worldPosition, state.setValue(LIT, true), 3);
            level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.75F, 1.0F);
        } else if (!entityNearby && lit) {
            level.setBlock(worldPosition, state.setValue(LIT, false), 3);
            level.playSound(null, worldPosition, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.75F, 1.0F);
        }

        if (!lit && !entityNearby) {
            double x = worldPosition.getX() + 0.5;
            double y = worldPosition.getY() + 0.5;
            double z = worldPosition.getZ() + 0.5;

            ((ServerLevel) level).sendParticles(
                    ParticleTypes.SCULK_CHARGE_POP,
                    x, y, z,
                    1,
                    0.1, 0.25, 0.1,
                    0
            );
        }
    }

    public boolean isAutoMode() { return autoMode; }

    public void setManualOverride(boolean manualOverride) {
        this.manualOverride = manualOverride;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void setManualOverrideTriggerPlayerNearby(boolean trigger) {
        this.manualOverrideTriggerPlayerNearby = trigger;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void setForcedLitState(boolean forced) {
        this.forcedLitState = forced;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void syncToClient() {
        if (this.level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public boolean isSlotBlocked(int slot) {
        return blockedSlots[slot];
    }

    public void toggleBlocked(int slot) {
        blockedSlots[slot] = !blockedSlots[slot];
        setChanged();
    }

    public void setBlocked(int slot, boolean blocked) {
        blockedSlots[slot] = blocked;
        setChanged();
    }

    private boolean hasNormalSculk() {
        ItemStack stack = items.getStackInSlot(3);
        return !stack.isEmpty() && stack.is(Items.SCULK_SENSOR);
    }

    private boolean hasCalibratedSculk() {
        ItemStack stack = items.getStackInSlot(3);
        return !stack.isEmpty() && stack.is(Items.CALIBRATED_SCULK_SENSOR);
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

        PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
        if (potionContents == null) return;

        Iterable<MobEffectInstance> effectsIterable = potionContents.getAllEffects();

        potionEffects[slot].clear();
        for (MobEffectInstance eff : effectsIterable) {
            potionEffects[slot].add(eff);
        }

        int maxDuration = 0;
        for (MobEffectInstance eff : effectsIterable) {
            maxDuration = Math.max(maxDuration, eff.getDuration());
        }

        potionDurations[slot] = maxDuration;
        potionTimers[slot] = maxDuration;
        potionColors[slot] = potionContents.getColor();

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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(tag, pRegistries);
        tag.put("Inventory", items.serializeNBT(pRegistries));

        tag.putBoolean("AutoMode", autoMode);
        tag.putBoolean("ManualOverride", manualOverride);
        tag.putBoolean("ManualOverrideTriggerPlayerNearby", manualOverrideTriggerPlayerNearby);
        tag.putBoolean("ForcedLitState", forcedLitState);

        tag.putIntArray("PotionDurations", potionDurations);
        tag.putIntArray("PotionTimers", potionTimers);
        tag.putIntArray("PotionColors", potionColors);

        // Save blocked slots
        int[] blocked = new int[blockedSlots.length];
        for (int i = 0; i < blockedSlots.length; i++) {
            blocked[i] = blockedSlots[i] ? 1 : 0;
        }
        tag.putIntArray("BlockedSlots", blocked);

        // Save effects
        for (int i = 0; i < potionEffects.length; i++) {
            ListTag listTag = new ListTag();
            for (MobEffectInstance effect : potionEffects[i]) {
                listTag.add(effect.save());
            }
            tag.put("PotionEffects" + i, listTag);
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(tag, pRegistries);
        items.deserializeNBT(pRegistries, tag.getCompound("Inventory"));

        autoMode = tag.getBoolean("AutoMode");
        manualOverride = tag.getBoolean("ManualOverride");
        manualOverrideTriggerPlayerNearby = tag.getBoolean("ManualOverrideTriggerPlayerNearby");
        forcedLitState = tag.getBoolean("ForcedLitState");

        int[] d = tag.getIntArray("PotionDurations");
        int[] t = tag.getIntArray("PotionTimers");
        int[] c = tag.getIntArray("PotionColors");

        System.arraycopy(d, 0, potionDurations, 0, Math.min(d.length, potionDurations.length));
        System.arraycopy(t, 0, potionTimers, 0, Math.min(t.length, potionTimers.length));
        System.arraycopy(c, 0, potionColors, 0, Math.min(c.length, potionColors.length));

        // Load blocked slots
        int[] blocked = tag.getIntArray("BlockedSlots");
        for (int i = 0; i < blockedSlots.length && i < blocked.length; i++) {
            blockedSlots[i] = blocked[i] == 1;
        }

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
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider pRegistries) {
        super.onDataPacket(net, pkt, pRegistries);
        if (level != null && level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, pRegistries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider holders) {
        this.loadAdditional(tag, holders);
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
