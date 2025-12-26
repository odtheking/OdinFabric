package com.odtheking.mixin.mixins;

import com.odtheking.odin.features.impl.render.RenderOptimizer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "tickDeath", at = @At("HEAD"), cancellable = true)
    public void hideDeathAnimation(CallbackInfo ci) {
        if (RenderOptimizer.hideEntityDeathAnimation()) {
            ci.cancel();

            LivingEntity self = (LivingEntity)(Object)this;
            self.remove(Entity.RemovalReason.DISCARDED);

            if (RenderOptimizer.hideDyingEntityArmorStand()) {
                Entity nextEntity = self.level().getEntity(self.getId() + 1);

                if (nextEntity instanceof ArmorStand) {
                    nextEntity.remove(Entity.RemovalReason.DISCARDED);
                }
            }
        }
    }
}
