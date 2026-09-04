package com.getitemfromblock.create_tweaked_controllers.input;

import com.getitemfromblock.create_tweaked_controllers.controller.TweakedLinkedControllerClientHandler;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

/**
 * Forge event subscriber for input-related events.
 * <p>
 * Handles the interaction key mapping trigger to deactivate the lectern controller
 * when the player clicks (e.g. left-click to break block), preventing the controller
 * from interfering with normal gameplay interactions.
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class ModInputEvents
{
    @SubscribeEvent
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event)
    {
        TweakedLinkedControllerClientHandler.deactivateInLectern();
    }
}
