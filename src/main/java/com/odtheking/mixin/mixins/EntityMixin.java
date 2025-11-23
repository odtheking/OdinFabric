package com.odtheking.mixin.mixins;

import com.odtheking.odin.features.impl.dungeon.Highlight;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    public void onGetTeamColor(CallbackInfoReturnable<Integer> cir) {
        Entity self = (Entity)(Object)this;

        Integer color = Highlight.getTeammateColor(self);
        if (color != null) cir.setReturnValue(color);
    }
}