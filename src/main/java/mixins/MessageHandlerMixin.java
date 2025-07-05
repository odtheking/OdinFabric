package mixins;

import me.odinmod.odin.features.impl.skyblock.PlayerDisplay;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(MessageHandler.class)
public class MessageHandlerMixin {

    @ModifyArg(method = "onGameMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;setOverlayMessage(Lnet/minecraft/text/Text;Z)V"), index = 0)
    private Text modifyOverlayMessage(Text original) {
        return PlayerDisplay.modifyText(original);
    }
}
