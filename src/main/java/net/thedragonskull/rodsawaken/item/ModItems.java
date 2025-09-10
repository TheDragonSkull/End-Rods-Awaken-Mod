package net.thedragonskull.rodsawaken.item;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thedragonskull.rodsawaken.RodsAwaken;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(RodsAwaken.MOD_ID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
