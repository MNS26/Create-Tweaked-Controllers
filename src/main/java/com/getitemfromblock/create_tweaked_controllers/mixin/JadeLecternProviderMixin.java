package com.getitemfromblock.create_tweaked_controllers.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.getitemfromblock.create_tweaked_controllers.block.TweakedLecternControllerBlockEntity;

import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;

/**
 * Mixin that prevents Jade from crashing when looking at a Tweaked Lectern Controller.
 * <p>
 * Jade's {@code LecternProvider.streamData()} unconditionally casts the block entity to
 * vanilla {@code LecternBlockEntity}, but our block entity extends Create's
 * {@code SmartBlockEntity} instead, causing a {@link ClassCastException}. This mixin
 * intercepts the call and returns {@link ItemStack#EMPTY} when the block entity is ours.
 * <p>
 * Registered as optional in the mixin config, so it is safely skipped without crashing
 * when Jade is not installed or if its API changes.
 */
@Mixin(targets = "snownee.jade.addon.vanilla.LecternProvider", remap = false)
public class JadeLecternProviderMixin
{
    @Inject(method = "streamData", at = @At("HEAD"), cancellable = true, require = 0)
    private void createTweakedControllers$onStreamData(BlockAccessor accessor, CallbackInfoReturnable<ItemStack> cir)
    {
        if (accessor.getBlockEntity() instanceof TweakedLecternControllerBlockEntity)
            cir.setReturnValue(ItemStack.EMPTY);
    }
}