package com.getitemfromblock.create_tweaked_controllers.compat.ComputerCraft;

import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.compat.computercraft.AttachedComputerPacket;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.createmod.catnip.platform.CatnipServices;

import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

/**
 * Abstract base class for CC:Tweaked peripherals that sync attachment state with the block entity.
 * <p>
 * Manages a list of attached computers and notifies the block entity when computers
 * connect/disconnect. Sends network packets to update the block entity's visual state
 * (e.g. the "has attached computer" indicator).
 *
 * @param <T> the block entity type this peripheral is attached to
 * @see ModComputerBehavior
 */
public abstract class ModSyncedPeripheral<T extends SmartBlockEntity> implements IPeripheral
{
    protected final T blockEntity;
    protected final CopyOnWriteArrayList<IComputerAccess> computers = new CopyOnWriteArrayList<>();

    public ModSyncedPeripheral(T blockEntity)
    {
        this.blockEntity = blockEntity;
    }

    @Override
    public void attach(@NotNull IComputerAccess computer)
    {
        boolean found = false;
        for (var comp : computers)
        {
            if (comp.getID() == computer.getID())
            {
                found = true;
                break;
            }
        }
        if (!found)
        {
            computers.add(computer);
        }
        updateBlockEntity();
    }

    @Override
    public void detach(@NotNull IComputerAccess computer)
    {
        computers.remove(computer);
        updateBlockEntity();
    }

    private void updateBlockEntity()
    {
        boolean hasAttachedComputer = !computers.isEmpty();

        blockEntity.getBehaviour(ModComputerBehavior.TYPE).setHasAttachedComputer(hasAttachedComputer);
        CatnipServices.NETWORK.sendToAllClients(new AttachedComputerPacket(blockEntity.getBlockPos(), hasAttachedComputer));
    }

    @Override
    public boolean equals(@Nullable IPeripheral other)
    {
        return this == other;
    }

}
