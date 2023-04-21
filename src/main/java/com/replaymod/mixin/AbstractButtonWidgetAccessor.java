package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.components.AbstractWidget;

@Mixin(AbstractWidget.class)
public interface AbstractButtonWidgetAccessor {
    @Accessor
    int getHeight();
}
