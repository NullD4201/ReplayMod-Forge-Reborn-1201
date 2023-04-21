package com.replaymod.gui.versions.callbacks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.gui.utils.Event;

public interface RenderHudCallback {
    Event<RenderHudCallback> EVENT = Event.create((listeners) ->
            (stack, partialTicks) -> {
                for (RenderHudCallback listener : listeners) {
                    listener.renderHud(stack, partialTicks);
                }
            }
    );

    void renderHud(PoseStack stack, float partialTicks);
}
