package com.getitemfromblock.create_tweaked_controllers.gui;


import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Custom inventory slot for the Tweaked Linked Controller's frequency configuration screen.
 * <p>
 * Extends NeoForge's {@link SlotItemHandler} with visibility control for the two-page
 * layout (buttons page vs axes page). Only one page of slots is visible at a time.
 *
 * @see TweakedLinkedControllerMenu
 */
public class ControllerItemSlot extends SlotItemHandler
{
    protected boolean active = true;

    public ControllerItemSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition)
    {
        super(itemHandler, index, xPosition, yPosition);
    }

    public ControllerItemSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, boolean active)
    {
        super(itemHandler, index, xPosition, yPosition);
        this.active = active;
    }

    @Override
    public boolean isActive()
    {
        return active;
    }

    public void SetActive(boolean active)
    {
        this.active = active;
    }
    
}
