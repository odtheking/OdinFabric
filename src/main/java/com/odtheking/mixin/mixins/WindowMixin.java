package com.odtheking.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.platform.Window;
import com.odtheking.odin.features.impl.floor7.TerminalSolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Window.class)
public abstract class WindowMixin {
    @Shadow
    public abstract int getWidth();

    @Shadow
    public abstract int getGuiScale();

    @Shadow
    public abstract int getHeight();

    @ModifyReturnValue(
            method = "getGuiScale",
            at = @At("RETURN")
    )
    private int getGuiScale(int original) {
        if (TerminalSolver.INSTANCE.getCurrentTerm() != null) {
            return TerminalSolver.INSTANCE.getNormalTermSize();
        }
        return original;
    }

    @ModifyReturnValue(
            method = "getGuiScaledWidth",
            at = @At("RETURN")
    )
    private int getGuiScaledWidth(int original) {
        if (TerminalSolver.INSTANCE.getCurrentTerm() != null) {
            int j = (int)((double)getWidth() / getGuiScale());
            return (double)getWidth() / getGuiScale() > (double)j ? j + 1 : j;
        }
        return original;
    }

    @ModifyReturnValue(
            method = "getGuiScaledHeight",
            at = @At("RETURN")
    )
    private int getGuiScaledHeight(int original) {
        if (TerminalSolver.INSTANCE.getCurrentTerm() != null) {
            int j = (int)((double)getHeight() / this.getGuiScale());
            return (double)getHeight() / this.getGuiScale() > (double)j ? j + 1 : j;
        }
        return original;
    }
}
