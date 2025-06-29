package mixins;

import me.odinmod.odin.events.GuiEvent;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.odinmod.odin.OdinMod.mc;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;draw()V", shift = At.Shift.AFTER))
    public void hookRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        Screen window = mc.currentScreen;
        if (window != null) (new GuiEvent.NVGRender(window)).postAndCatch();
    }
}