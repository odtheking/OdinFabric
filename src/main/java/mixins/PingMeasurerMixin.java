package mixins;

import net.minecraft.client.network.PingMeasurer;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.odinmod.odin.utils.ServerUtils.currentPing;

@Mixin(PingMeasurer.class)
public class PingMeasurerMixin {
    @Inject(method = "onPingResult", at = @At("HEAD"))
    public void onPingResult(PingResultS2CPacket packet, CallbackInfo ci) {
        currentPing = Math.toIntExact(Util.getMeasuringTimeMs() - packet.startTime());
    }
}
