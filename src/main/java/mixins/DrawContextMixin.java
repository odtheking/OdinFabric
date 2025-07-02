package mixins;

import me.odinmod.odin.events.GuiEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class DrawContextMixin {
    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    public void onDrawStackOverlay(DrawContext context, Slot slot, CallbackInfo ci) {
        if (new GuiEvent.DrawSlotOverlay(context, slot.getStack(), slot.x, slot.y).postAndCatch()) ci.cancel();
    }
}