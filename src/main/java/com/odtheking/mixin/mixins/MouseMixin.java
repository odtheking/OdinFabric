package com.odtheking.mixin.mixins;

import com.odtheking.odin.OdinMod;
import com.odtheking.odin.features.impl.skyblock.NoCursorReset;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow
    private double x;
    @Shadow
    private double y;

    @Unique
    private double beforeX;
    @Unique
    private double beforeY;

    @Inject(method = "lockCursor", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Mouse;x:D", ordinal = 0))
    private void odin$lockXPos(CallbackInfo ci) {
        this.beforeX = this.x;
        this.beforeY = this.y;
    }

    @Inject(method = "unlockCursor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;getHandle()J"))
    private void odin$correctCursorPosition(CallbackInfo ci) {
        if (OdinMod.INSTANCE.getMc().currentScreen instanceof GenericContainerScreen && NoCursorReset.shouldHookMouse()) {
            InputUtil.setCursorParameters(
                OdinMod.INSTANCE.getMc().getWindow().getHandle(), InputUtil.GLFW_CURSOR_NORMAL,
                this.beforeX, this.beforeY
            );
            this.x = this.beforeX;
            this.y = this.beforeY;
        }
    }
}