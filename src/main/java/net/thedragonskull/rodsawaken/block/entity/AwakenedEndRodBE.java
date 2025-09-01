package net.thedragonskull.rodsawaken.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AwakenedEndRodBE extends BlockEntity {

    public AwakenedEndRodBE(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.AWAKENED_END_ROD_BE.get(), pPos, pBlockState);
    }

    public void tick() {

    }

}
