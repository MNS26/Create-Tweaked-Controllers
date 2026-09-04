package com.getitemfromblock.create_tweaked_controllers.compat.ComputerCraft;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.getitemfromblock.create_tweaked_controllers.block.TweakedLecternControllerBlockEntity;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.world.entity.player.Player;

/**
 * CC:Tweaked peripheral for the Tweaked Lectern Controller block.
 * <p>
 * Exposes controller state to Lua programs running on attached computers:
 * <ul>
 *   <li>{@code hasUser()} - whether a player is currently using the controller</li>
 *   <li>{@code getUserUUID()} - UUID of the current user</li>
 *   <li>{@code getButton(index)} - read button state (1-indexed, range [1,15])</li>
 *   <li>{@code getAxis(index)} - read axis value (1-indexed, range [1,6])</li>
 *   <li>{@code setFullPrecision(bool)} - enable/disable float-level axis precision</li>
 *   <li>{@code isFullPrecision()} - check if full precision mode is active</li>
 * </ul>
 * Fires events: {@code controller_start_using} and {@code controller_stop_using}.
 *
 * @see TweakedLecternControllerBlockEntity
 */
public class TweakedLecternPeripheral extends ModSyncedPeripheral<TweakedLecternControllerBlockEntity>
{
    public TweakedLecternPeripheral(TweakedLecternControllerBlockEntity be)
    {
        super(be);
        be.AssignPeripheral(this);
    }

    @NotNull
    @LuaFunction
    public final boolean hasUser()
    {
        return blockEntity.hasUser();
    }

    @LuaFunction
    public final String getUserUUID()
    {
        UUID result = blockEntity.getUserUUID();
        return result == null ? null : result.toString();
    }

    @LuaFunction
    public final boolean getButton(int buttonIndex) throws LuaException
    {
        buttonIndex--;
        if (buttonIndex < 0 || buttonIndex > 14) throw new LuaException("Index out of range : [1,15]");
        return blockEntity.GetButton(buttonIndex);
    }

    @LuaFunction
    public final float getAxis(int axisIndex) throws LuaException
    {
        axisIndex--;
        if (axisIndex < 0 || axisIndex > 5) throw new LuaException("Index out of range : [1,6]");
        return blockEntity.GetAxis(axisIndex);
    }

    @LuaFunction
    public final void setFullPrecision(boolean value)
    {
        blockEntity.SetFullPrecision(value);
    }

    @NotNull
    @LuaFunction
    public final boolean isFullPrecision()
    {
        return blockEntity.shouldUseFullPrecision();
    }

    public void NotifyUseEvent(boolean use, Player player)
    {
        for (IComputerAccess computer : computers)
        {
            Object arg = player == null ? null : player.getUUID().toString();
            if (use)
            {
                computer.queueEvent("controller_start_using", arg);
            }
            else
            {
                computer.queueEvent("controller_stop_using", arg);
            }
        }
    }
    
    @NotNull
    @Override
    public String getType()
    {
        return "tweaked_controller";
    }
}