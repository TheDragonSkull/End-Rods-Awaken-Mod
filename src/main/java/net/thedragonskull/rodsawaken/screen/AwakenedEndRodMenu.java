package net.thedragonskull.rodsawaken.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.thedragonskull.rodsawaken.block.ModBlocks;
import net.thedragonskull.rodsawaken.block.entity.AwakenedEndRodBE;

import java.util.Objects;

public class AwakenedEndRodMenu extends AbstractContainerMenu {

    private final AwakenedEndRodBE blockEntity;
    private final ContainerLevelAccess access;

    public AwakenedEndRodMenu(int id, Inventory playerInv, AwakenedEndRodBE blockEntity) {
        super(ModMenuTypes.AWAKENED_END_ROD_MENU.get(), id);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(Objects.requireNonNull(blockEntity.getLevel()), blockEntity.getBlockPos());

        // General Inventory
        ItemStackHandler handler = blockEntity.getItems();

        // Potion slots (0-2)
        this.addSlot(new SlotItemHandler(handler, 0, 31, 42));
        this.addSlot(new SlotItemHandler(handler, 1, 65, 42));
        this.addSlot(new SlotItemHandler(handler, 2, 99, 42));

        // Sensor slot (3)
        this.addSlot(new SlotItemHandler(handler, 3, 129, 30));

        // Inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    public AwakenedEndRodMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, (AwakenedEndRodBE) playerInv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.AWAKENED_END_ROD.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            int containerSlots = 4;

            if (index < containerSlots) {
                // BE -> inv
                if (!this.moveItemStackTo(stackInSlot, containerSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // inv -> BE
                if (!this.moveItemStackTo(stackInSlot, 0, containerSlots, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    public AwakenedEndRodBE getBlockEntity() {
        return blockEntity;
    }
}
