package com.getitemfromblock.create_tweaked_controllers.packet;

import com.getitemfromblock.create_tweaked_controllers.block.TweakedLecternControllerBlockEntity;
import com.getitemfromblock.create_tweaked_controllers.item.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Abstract base class for all Tweaked Linked Controller network packets.
 * <p>
 * Handles the common packet structure:
 * <ul>
 *   <li>A flag byte encoding whether the packet targets a lectern and uses full precision</li>
 *   <li>An optional lectern block position (3 ints) if targeting a lectern controller</li>
 *   <li>Subclass-specific payload data</li>
 * </ul>
 * The {@link #handle(IPayloadContext)} method dispatches to either
 * {@link #handleItem(ServerPlayer, ItemStack)} (for handheld controller) or
 * {@link #handleLectern(ServerPlayer, TweakedLecternControllerBlockEntity)} (for lectern).
 *
 * @see TweakedLinkedControllerButtonPacket
 * @see TweakedLinkedControllerAxisPacket
 * @see TweakedLinkedControllerBindPacket
 * @see TweakedLinkedControllerStopLecternPacket
 */
public abstract class TweakedLinkedControllerPacketBase implements CustomPacketPayload
{
    protected final BlockPos lecternPos;
    protected final boolean useFullPrecision;

    protected TweakedLinkedControllerPacketBase(BlockPos lecternPos, boolean useFullPrecision)
    {
        this.lecternPos = lecternPos;
        this.useFullPrecision = useFullPrecision;
    }

    protected boolean inLectern()
    {
        return lecternPos != null;
    }

    public static void writeBase(FriendlyByteBuf buffer, TweakedLinkedControllerPacketBase pkt)
    {
        byte mask = (byte) ((pkt.inLectern() ? 1 : 0) | (pkt.useFullPrecision ? 2 : 0));
        buffer.writeByte(mask);
        if (pkt.inLectern())
        {
            buffer.writeInt(pkt.lecternPos.getX());
            buffer.writeInt(pkt.lecternPos.getY());
            buffer.writeInt(pkt.lecternPos.getZ());
        }
    }

    public record BaseFields(BlockPos lecternPos, boolean useFullPrecision) {}

    public static BaseFields readBase(FriendlyByteBuf buffer)
    {
        byte val = buffer.readByte();
        BlockPos pos = null;
        if ((val & 0x1) != 0)
        {
            pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
        }
        return new BaseFields(pos, (val & 0x2) != 0);
    }

    public void handle(IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player))
                return;

            if (inLectern())
            {
                BlockEntity be = player.level().getBlockEntity(lecternPos);
                if (!(be instanceof TweakedLecternControllerBlockEntity))
                    return;
                handleLectern(player, (TweakedLecternControllerBlockEntity) be);
            }
            else
            {
                ItemStack controller = player.getMainHandItem();
                if (!ModItems.TWEAKED_LINKED_CONTROLLER.isIn(controller))
                {
                    controller = player.getOffhandItem();
                    if (!ModItems.TWEAKED_LINKED_CONTROLLER.isIn(controller))
                        return;
                }
                handleItem(player, controller);
            }
        });
    }

    protected abstract void handleItem(ServerPlayer player, ItemStack heldItem);
    protected abstract void handleLectern(ServerPlayer player, TweakedLecternControllerBlockEntity lectern);
}
