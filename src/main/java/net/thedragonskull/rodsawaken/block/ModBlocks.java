package net.thedragonskull.rodsawaken.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thedragonskull.rodsawaken.RodsAwaken;
import net.thedragonskull.rodsawaken.block.custom.AwakenedEndRod;
import net.thedragonskull.rodsawaken.item.ModItems;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(RodsAwaken.MOD_ID);

    public static final DeferredBlock<Block> AWAKENED_END_ROD = registerBlock("awakened_end_rod",
            () -> new AwakenedEndRod(BlockBehaviour.Properties.ofFullCopy(Blocks.END_ROD).lightLevel(litRodEmission())));




    private static ToIntFunction<BlockState> litRodEmission() {
        return (p_50763_) -> {
            return p_50763_.getValue(AwakenedEndRod.LIT) ? 14 : 0;
        };
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
