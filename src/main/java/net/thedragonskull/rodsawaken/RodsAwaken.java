package net.thedragonskull.rodsawaken;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.thedragonskull.rodsawaken.block.ModBlocks;
import net.thedragonskull.rodsawaken.block.entity.ModBlockEntities;
import net.thedragonskull.rodsawaken.item.ModItems;
import net.thedragonskull.rodsawaken.network.PacketHandler;
import net.thedragonskull.rodsawaken.particle.ModParticles;
import net.thedragonskull.rodsawaken.screen.AwakenedEndRodScreen;
import net.thedragonskull.rodsawaken.screen.ModMenuTypes;
import org.slf4j.Logger;

@Mod(RodsAwaken.MOD_ID)
public class RodsAwaken {
    public static final String MOD_ID = "rodsawaken";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RodsAwaken(IEventBus modEventBus, ModContainer container) {

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModParticles.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.insertAfter(Blocks.END_ROD.asItem().getDefaultInstance(),
                    ModBlocks.AWAKENED_END_ROD.get().asItem().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MenuScreens.register(ModMenuTypes.AWAKENED_END_ROD_MENU.get(), AwakenedEndRodScreen::new);

            event.enqueueWork(PacketHandler::register);
        }
    }
}
