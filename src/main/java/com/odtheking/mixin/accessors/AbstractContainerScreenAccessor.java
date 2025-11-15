package com.odtheking.mixin.accessors;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("hoveredSlot")
    @Nullable
    Slot getHoveredSlot();

    @Accessor("leftPos")
    int getX();

    @Accessor("topPos")
    int getY();

    @Accessor("imageWidth")
    int getWidth();

    @Accessor("imageHeight")
    int getHeight();
}
