package mixins;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PingMeasurer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPlayNetworkHandler.class)
public interface ClientPlayNetworkHandlerAccessor {
    @Accessor("pingMeasurer")
    PingMeasurer getPingMeasurer();
}
