package net.thedragonskull.rodsawaken.event;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.thedragonskull.rodsawaken.RodsAwaken;
import net.thedragonskull.rodsawaken.block.ModBlocks;
import net.thedragonskull.rodsawaken.block.entity.AwakenedEndRodBE;
import net.thedragonskull.rodsawaken.block.entity.ModBlockEntities;
import net.thedragonskull.rodsawaken.particle.ModParticles;
import net.thedragonskull.rodsawaken.particle.custom.AwakenedEndRodGlitterParticles;
import net.thedragonskull.rodsawaken.screen.AwakenedEndRodScreen;
import net.thedragonskull.rodsawaken.screen.ModMenuTypes;
import net.thedragonskull.rodsawaken.util.SensorSlotTooltip;

@EventBusSubscriber(modid = RodsAwaken.MOD_ID)
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

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.AWAKENED_END_ROD_GLITTER.get(), AwakenedEndRodGlitterParticles.Provider::new);
    }

    @SubscribeEvent
    public static void registerTooltipComponent(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(SensorSlotTooltip.class, component -> component);
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.AWAKENED_END_ROD_MENU.get(), AwakenedEndRodScreen::new);
    }

    @SubscribeEvent
    public static void registerCapability(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.AWAKENED_END_ROD_BE.get(),
                (be, side) -> be.hopperItemHandler
        );
    }
}
