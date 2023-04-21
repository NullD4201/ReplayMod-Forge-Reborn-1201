package com.replaymod.core.gui;

import com.replaymod.core.ReplayMod;
import com.replaymod.gui.GuiRenderer;
import com.replaymod.gui.RenderInfo;
import com.replaymod.gui.element.GuiButton;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;

public class GuiReplayButton extends GuiButton {
    @Override
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        super.draw(renderer, size, renderInfo);

        renderer.bindTexture(ReplayMod.LOGO_FAVICON);
        renderer.drawTexturedRect(3, 3, 0, 0, size.getWidth() - 6, size.getHeight() - 6, 1, 1, 1, 1);
    }
}
