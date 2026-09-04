package com.getitemfromblock.create_tweaked_controllers.packet;

import com.getitemfromblock.create_tweaked_controllers.CreateTweakedControllers;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registry and dispatcher for all network packets used by the mod.
 * <p>
 * All packets are client-to-server (play-to-server) and use NeoForge's payload system.
 * The four packet types are:
 * <ul>
 *   <li>{@link TweakedLinkedControllerButtonPacket} - packed button states (short)</li>
 *   <li>{@link TweakedLinkedControllerAxisPacket} - packed or full-precision axis states</li>
 *   <li>{@link TweakedLinkedControllerBindPacket} - bind a controller input to a Redstone Link</li>
 *   <li>{@link TweakedLinkedControllerStopLecternPacket} - notify server the player stopped using lectern</li>
 * </ul>
 * Uses a versioned network protocol (version "2") to ensure client/server compatibility.
 */
public class ModPackets
{
    public static final String NETWORK_VERSION = "3";

    public static void registerBusListener(IEventBus modEventBus)
    {
        modEventBus.addListener(ModPackets::register);
    }

    private static void register(final RegisterPayloadHandlersEvent event)
    {
        PayloadRegistrar registrar = event.registrar(CreateTweakedControllers.ID).versioned(NETWORK_VERSION);
        registrar.playToServer(TweakedLinkedControllerButtonPacket.TYPE,
            TweakedLinkedControllerButtonPacket.STREAM_CODEC,
            TweakedLinkedControllerPacketBase::handle);
        registrar.playToServer(TweakedLinkedControllerAxisPacket.TYPE,
            TweakedLinkedControllerAxisPacket.STREAM_CODEC,
            TweakedLinkedControllerPacketBase::handle);
        registrar.playToServer(TweakedLinkedControllerBindPacket.TYPE,
            TweakedLinkedControllerBindPacket.STREAM_CODEC,
            TweakedLinkedControllerPacketBase::handle);
        registrar.playToServer(TweakedLinkedControllerStopLecternPacket.TYPE,
            TweakedLinkedControllerStopLecternPacket.STREAM_CODEC,
            TweakedLinkedControllerPacketBase::handle);
        registrar.playToClient(TweakedLinkedControllerInputSyncPacket.TYPE,
            TweakedLinkedControllerInputSyncPacket.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(payload::handle));
    }

    public static void sendToServer(CustomPacketPayload payload)
    {
        PacketDistributor.sendToServer(payload);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload)
    {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendToNear(Level world, BlockPos pos, int range, CustomPacketPayload payload)
    {
        if (!(world instanceof ServerLevel serverLevel))
            return;
        PacketDistributor.sendToPlayersNear(serverLevel, null, pos.getX(), pos.getY(), pos.getZ(), range, payload);
    }
}
