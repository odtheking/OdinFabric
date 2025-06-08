package mixins;

import me.odinmod.odin.features.skyblock.NoCursorReset;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.odinmod.odin.OdinMod.mc;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow private double x;
    @Shadow private double y;
    @Unique
    private double beforeX;
    @Unique
    private double beforeY;

    @Inject(method = "lockCursor", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Mouse;cursorLocked:Z", ordinal = 1))
    private void odin$lockXPos(CallbackInfo ci) {
        this.beforeX = this.x;
        this.beforeY = this.y;
    }

    @Inject(method = "unlockCursor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/InputUtil;setCursorParameters(JIDD)V", ordinal = 0, shift = At.Shift.AFTER))
    private void odin$correctCursorPosition(CallbackInfo ci) {
        if (mc.currentScreen instanceof GenericContainerScreen && NoCursorReset.shouldHookMouse()) GLFW.glfwSetCursorPos(mc.getWindow().getHandle(), this.beforeX, this.beforeY);
    }
}