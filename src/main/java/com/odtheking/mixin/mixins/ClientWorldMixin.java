package com.odtheking.mixin.mixins;

import com.odtheking.odin.events.EntityLeaveWorldEvent;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {

    @Inject(method = "removeEntity", at = @At("HEAD"), cancellable = true)
    private void onRemoveEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci) {
       Entity entity = ((ClientWorld) (Object) this).getEntityById(entityId);
       if (entity != null && (new EntityLeaveWorldEvent(entity, removalReason).postAndCatch())) ci.cancel();
    }
}
