package com.getitemfromblock.create_tweaked_controllers.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

/**
 * Mixin that disables Create's flicker score mechanic for kinetic block entities.
 * <p>
 * The flicker score was causing issues when Redstone Link networks were updated too
 * rapidly by the controller's axis outputs, leading to visual artifacts and potential
 * performance problems. By always returning 0, kinetic blocks treat all signal changes
 * as stable.
 */
@Mixin(KineticBlockEntity.class)
public class KineticBlockEntityMixin
{
    @Inject(method = "getFlickerScore", at = @At("HEAD"), cancellable = true, remap = false)
    private void getFlickerScoreMixin(CallbackInfoReturnable<Integer> callback)
    {
        callback.setReturnValue(0);
    }
}