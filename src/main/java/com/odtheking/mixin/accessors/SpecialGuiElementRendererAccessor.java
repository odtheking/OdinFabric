package com.odtheking.mixin.accessors;

import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpecialGuiElementRenderer.class)
public interface SpecialGuiElementRendererAccessor {
    @Accessor("textureView")
    GpuTextureView getTextureView();
}