package mixins;

import io.github.odtheking.odin.OdinMod;
import io.github.odtheking.odin.clickgui.ClickGUI;
import io.github.odtheking.odin.events.GuiEvent;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;draw()V", shift = At.Shift.AFTER))
    public void hookRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (OdinMod.INSTANCE.getMc().currentScreen instanceof ClickGUI clickGUI) {
            new GuiEvent.NVGRender(clickGUI).postAndCatch();
        }
    }
}