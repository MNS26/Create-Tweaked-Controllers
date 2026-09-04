package com.getitemfromblock.create_tweaked_controllers.item;

import com.simibubi.create.foundation.data.AssetLookup;
import com.tterrag.registrate.util.entry.ItemEntry;

import com.getitemfromblock.create_tweaked_controllers.CreateTweakedControllers;

/**
 * Registry for items added by the mod.
 * <p>
 * Registers the {@link TweakedLinkedControllerItem} with properties:
 * stack size of 1 (single controller per stack) and custom item model rendering.
 */
public class ModItems
{
    public static final ItemEntry<TweakedLinkedControllerItem> TWEAKED_LINKED_CONTROLLER =
            CreateTweakedControllers.registrate().item("tweaked_linked_controller", TweakedLinkedControllerItem::new)
                    .properties(p -> p.stacksTo(1))
                    .model(AssetLookup.itemModelWithPartials())
                    .register();

    public static void register() {}
}