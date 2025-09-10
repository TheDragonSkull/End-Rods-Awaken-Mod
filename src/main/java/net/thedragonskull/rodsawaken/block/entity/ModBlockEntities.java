package net.thedragonskull.rodsawaken.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thedragonskull.rodsawaken.RodsAwaken;
import net.thedragonskull.rodsawaken.block.ModBlocks;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RodsAwaken.MOD_ID);

    public static final Supplier<BlockEntityType<AwakenedEndRodBE>> AWAKENED_END_ROD_BE =
            BLOCK_ENTITIES.register("awakened_end_rod_be", () ->
                    BlockEntityType.Builder.of(AwakenedEndRodBE::new,
                            ModBlocks.AWAKENED_END_ROD.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
