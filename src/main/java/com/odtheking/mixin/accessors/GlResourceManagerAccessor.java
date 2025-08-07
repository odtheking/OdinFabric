package com.odtheking.mixin.accessors;

import net.minecraft.client.gl.GlResourceManager;
import net.minecraft.client.gl.ShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlResourceManager.class)
public interface GlResourceManagerAccessor {

    @Accessor("currentProgram")
    ShaderProgram currentProgram();
}