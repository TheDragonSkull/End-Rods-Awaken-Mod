package net.thedragonskull.rodsawaken.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.thedragonskull.rodsawaken.RodsAwaken;

@EventBusSubscriber(modid = RodsAwaken.MOD_ID)
public class PayloadRegister {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                C2SToggleBlockedSlotPacket.TYPE,
                C2SToggleBlockedSlotPacket.STREAM_CODEC,
                ServerPayloadHandler.getInstance()::handleToggleBlockedSlot
        );

        registrar.playToServer(
                C2SClearPotionSlotPacket.TYPE,
                C2SClearPotionSlotPacket.STREAM_CODEC,
                ServerPayloadHandler.getInstance()::handleClearPotionSlot
        );
    }

}
