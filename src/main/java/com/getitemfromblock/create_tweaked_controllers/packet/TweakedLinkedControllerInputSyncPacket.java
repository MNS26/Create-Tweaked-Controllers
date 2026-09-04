package com.getitemfromblock.create_tweaked_controllers.packet;

import java.util.UUID;

import com.getitemfromblock.create_tweaked_controllers.CreateTweakedControllers;
import com.getitemfromblock.create_tweaked_controllers.controller.InputSnapshot;
import com.getitemfromblock.create_tweaked_controllers.controller.TweakedLinkedControllerClientHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Packet sent from server to client carrying another player's controller input state.
 * <p>
 * Used so clients can render the lectern controller with the inputs of a remote player
 * who is currently using it. Carries the owning player's UUID plus the packed button
 * and axis states.
 *
 * @see TweakedLinkedControllerServerHandler
 * @see TweakedLinkedControllerClientHandler
 */
public class TweakedLinkedControllerInputSyncPacket implements CustomPacketPayload
{
    public static final Type<TweakedLinkedControllerInputSyncPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(CreateTweakedControllers.ID, "controller_input_sync"));

    public static final StreamCodec<FriendlyByteBuf, TweakedLinkedControllerInputSyncPacket> STREAM_CODEC =
        StreamCodec.of(
            (buf, pkt) -> {
                buf.writeUUID(pkt.playerUUID);
                buf.writeShort(pkt.playerInput.buttons());
                buf.writeInt(pkt.playerInput.axis());
            },
            buf -> new TweakedLinkedControllerInputSyncPacket(buf.readUUID(), buf.readShort(), buf.readInt()));

    final UUID playerUUID;
    private final InputSnapshot playerInput;

    public TweakedLinkedControllerInputSyncPacket(UUID playerUUID, short buttons, int axis)
    {
        this.playerUUID = playerUUID;
        this.playerInput = new InputSnapshot(buttons, axis, System.currentTimeMillis());
    }

    public InputSnapshot getInput()
    {
        return playerInput;
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public void handle()
    {
        TweakedLinkedControllerClientHandler.remoteInputs.put(playerUUID, playerInput);
    }
}
