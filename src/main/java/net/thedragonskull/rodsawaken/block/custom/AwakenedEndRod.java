package net.thedragonskull.rodsawaken.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.thedragonskull.rodsawaken.block.entity.AwakenedEndRodBE;
import org.jetbrains.annotations.Nullable;

public class AwakenedEndRod extends EndRodBlock implements EntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public AwakenedEndRod(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(LIT, Boolean.TRUE)
                        .setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(LIT, FACING);
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {

        if (!pState.getValue(LIT)) return;

        super.animateTick(pState, pLevel, pPos, pRandom);
    }

    @Override
    public InteractionResult use(BlockState pState, Level level, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {

        if (pPlayer.isSecondaryUseActive()) {
            if (!level.isClientSide) {
                boolean lit = pState.getValue(LIT);
                BlockState newState = pState.setValue(LIT, !lit);
                level.setBlock(pPos, newState, 3);
            }

            return InteractionResult.SUCCESS;
        }

        return super.use(pState, level, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new AwakenedEndRodBE(pPos, pState);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof AwakenedEndRodBE rod) rod.tick();
        };
    }
}
