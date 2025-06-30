package mixins;

import me.odinmod.odin.events.GuiEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HandledScreen.class)
public class HandledScreenMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    protected void onRender(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (new GuiEvent.RenderScreen((Screen)(Object) this, context, mouseX, mouseY).postAndCatch()) ci.cancel();
    }
    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    public void onMouseClickedSlot(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (new GuiEvent.MouseClick((Screen)(Object) this, slotId, button).postAndCatch()) ci.cancel();
    }
}
