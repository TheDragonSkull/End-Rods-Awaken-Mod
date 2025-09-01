package net.thedragonskull.rodsawaken.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.thedragonskull.rodsawaken.RodsAwaken;
import net.thedragonskull.rodsawaken.block.ModBlocks;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RodsAwaken.MOD_ID);

    public static final RegistryObject<BlockEntityType<AwakenedEndRodBE>> AWAKENED_END_ROD_BE =
            BLOCK_ENTITIES.register("awakened_end_rod_be", () ->
                    BlockEntityType.Builder.of(AwakenedEndRodBE::new,
                            ModBlocks.AWAKENED_END_ROD.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
