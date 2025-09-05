package net.thedragonskull.rodsawaken.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
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

        BlockEntity be = pLevel.getBlockEntity(pPos);
        for (int i = 0; i < 3; i++) {
            if (be instanceof AwakenedEndRodBE rodBE && rodBE.hasEffectInSlot(i)) {
                return;
            }
        }

        super.animateTick(pState, pLevel, pPos, pRandom);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);

            if (player.isSecondaryUseActive()) {
                boolean lit = state.getValue(LIT);
                boolean newLit = !lit;
                level.setBlock(pos, state.setValue(LIT, newLit), 3);

                if (lit) {
                    level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.75F, 1.0F);
                } else {
                    level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.75F, 1.0F);
                }

                if (be instanceof AwakenedEndRodBE rod) {
                    if (rod.isAutoMode()) {
                        boolean playerNearbyNow = !level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(4)).isEmpty();

                        rod.setManualOverride(true);
                        rod.setForcedLitState(newLit);
                        rod.setManualOverrideTriggerPlayerNearby(playerNearbyNow);
                    }
                }
            } else {
                if (be instanceof AwakenedEndRodBE rodBE) {
                    NetworkHooks.openScreen((ServerPlayer) player, rodBE, pos);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AwakenedEndRodBE awakened) {
                Containers.dropContents(level, pos, awakened.asContainer());
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
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
