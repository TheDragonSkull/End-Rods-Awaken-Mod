package net.thedragonskull.rodsawaken.event;

import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.thedragonskull.rodsawaken.RodsAwaken;
import net.thedragonskull.rodsawaken.block.ModBlocks;
import net.thedragonskull.rodsawaken.block.entity.AwakenedEndRodBE;

@Mod.EventBusSubscriber(modid = RodsAwaken.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    public static void onRegisterColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                (pState, pLevel, pPos, pTintIndex) -> {
                    if (pLevel != null && pPos != null) {
                        BlockEntity be = pLevel.getBlockEntity(pPos);
                        if (be instanceof AwakenedEndRodBE rodBE) {
                            return rodBE.getCombinedPotionColor();
                        }
                    }

                    return 0xFFFFFFFF;
                },
                ModBlocks.AWAKENED_END_ROD.get()
        );
    }

}
