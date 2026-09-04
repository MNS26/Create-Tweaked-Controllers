package com.getitemfromblock.create_tweaked_controllers;

import com.getitemfromblock.create_tweaked_controllers.input.MouseCursorHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-side initialization helper.
 * <p>
 * Registers the {@link MouseCursorHandler#InitValues()} call during FML client setup
 * to initialize mouse tracking state.
 */
public class ModClientStuff
{
    public static void onConstructor(IEventBus modEventBus)
    {
        modEventBus.addListener(ModClientStuff::clientInit);
    }

    public static void clientInit(final FMLClientSetupEvent event)
    {
        MouseCursorHandler.InitValues();
    }
}
