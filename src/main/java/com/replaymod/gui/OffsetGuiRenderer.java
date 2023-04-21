/*
 * This file is part of jGui API, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016 johni0702 <https://github.com/johni0702>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.replaymod.gui;

import java.util.ArrayDeque;
import java.util.Objects;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.gui.utils.NonNull;

import de.johni0702.minecraft.gui.utils.lwjgl.Point;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import net.minecraft.resources.ResourceLocation;

public class OffsetGuiRenderer implements com.replaymod.gui.GuiRenderer {

    @NonNull
    private final com.replaymod.gui.GuiRenderer renderer;

    @NonNull
    private final ReadablePoint position;

    @NonNull
    private final ReadableDimension size;

    private final boolean strict;

    /**
     * Creates a new strict offset jgui renderer with the same settings as the supplied one.
     *
     * @param renderer The renderer to copy from
     * @see #OffsetGuiRenderer(com.replaymod.gui.GuiRenderer, ReadablePoint, ReadableDimension, boolean)
     */
    public OffsetGuiRenderer(OffsetGuiRenderer renderer) {
        this(renderer.renderer, renderer.position, renderer.size, true);
    }

    /**
     * Create a new offset GUI renderer. All calls to this renderer are forwarded to the parent
     * renderer with coordinates offset by the specified position.
     * This also ensures that drawing happens within the specified bounds.
     *
     * @param renderer The parent renderer
     * @param position The position of this renderer within the parent (used as is, not copied)
     * @param size     The size of the drawable area (used as is, not copied)
     */
    public OffsetGuiRenderer(com.replaymod.gui.GuiRenderer renderer, ReadablePoint position, ReadableDimension size) {
        this(renderer, position, size, true);
    }


    /**
     * Create a new offset GUI renderer. All calls to this renderer are forwarded to the parent
     * renderer with coordinates offset by the specified position.
     * If strict is {@code true}, this also ensures that drawing happens within the specified bounds.
     *
     * @param renderer The parent renderer
     * @param position The position of this renderer within the parent (used as is, not copied)
     * @param size     The size of the drawable area (used as is, not copied)
     * @param strict   Whether drawing should be forced to be within the drawable area
     */
    public OffsetGuiRenderer(GuiRenderer renderer, ReadablePoint position, ReadableDimension size, boolean strict) {
        this.renderer = renderer;
        this.position = position;
        this.size = size;
        this.strict = strict;
    }

    @Override
    public ReadablePoint getOpenGlOffset() {
        ReadablePoint parentOffset = renderer.getOpenGlOffset();
        return new Point(parentOffset.getX() + position.getX(), parentOffset.getY() + position.getY());
    }

    @Override
    public PoseStack getMatrixStack() {
        return renderer.getMatrixStack();
    }

    @Override
    public ReadableDimension getSize() {
        return size;
    }

    @Override
    public void setDrawingArea(int x, int y, int width, int height) {
        if (!strict) {
            renderer.setDrawingArea(x + position.getX(), y + position.getY(), width, height);
            return;
        }
        int x2 = x + width;
        int y2 = y + height;
        // Convert and clamp top and left border
        x = Math.max(0, x + position.getX());
        y = Math.max(0, y + position.getY());
        // Clamp and convert bottom and right border
        x2 = Math.min(x2, size.getWidth()) + position.getX();
        y2 = Math.min(y2, size.getHeight()) + position.getY();
        // Make sure bottom and top / right and left aren't flipped
        x2 = Math.max(x2, x);
        y2 = Math.max(y2, y);
        // Pass to parent
        renderer.setDrawingArea(x, y, x2 - x, y2 - y);
    }

    public void startUsing() {
    	pushScissorState();
        setDrawingArea(0, 0, size.getWidth(), size.getHeight());
    }

    public void stopUsing() {
    	popScissorState();
    }
    
    private static final ArrayDeque<ScissorBounds> scissorStateStack = new ArrayDeque<>();
    private static ScissorBounds scissorState = ScissorBounds.DISABLED;

    public static void pushScissorState() {
        scissorStateStack.push(scissorState);
    }

    public static void popScissorState() {
        setScissorBounds(scissorStateStack.pop());
    }
    
    public static void setScissorDisabled() {
        setScissorBounds(ScissorBounds.DISABLED);
    }
    
    private static class ScissorBounds {
        private static final ScissorBounds DISABLED = new ScissorBounds(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        private ScissorBounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ScissorBounds that = (ScissorBounds) o;
            return x == that.x && y == that.y && width == that.width && height == that.height;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, width, height);
        }
    }
    
    private static void setScissorBounds(ScissorBounds newState) {
        ScissorBounds oldState = scissorState;
        if (Objects.equals(oldState, newState)) {
            return;
        }

        scissorState = newState;

        boolean isEnabled = newState != ScissorBounds.DISABLED;
        boolean wasEnabled = oldState != ScissorBounds.DISABLED;

        if (isEnabled) {
            if (!wasEnabled) {
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
            }
            GL11.glScissor(scissorState.x, scissorState.y, scissorState.width, scissorState.height);
        } else {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    @Override
    public void bindTexture(ResourceLocation location) {
        renderer.bindTexture(location);
    }

    @Override
    public void bindTexture(int glId) {
        renderer.bindTexture(glId);
    }

    @Override
    public void drawTexturedRect(int x, int y, int u, int v, int width, int height) {
        renderer.drawTexturedRect(x + position.getX(), y + position.getY(), u, v, width, height);
    }

    @Override
    public void drawTexturedRect(int x, int y, int u, int v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        renderer.drawTexturedRect(x + position.getX(), y + position.getY(), u, v, width, height, uWidth, vHeight, textureWidth, textureHeight);
    }

    @Override
    public void drawRect(int x, int y, int width, int height, int color) {
        renderer.drawRect(x + position.getX(), y + position.getY(), width, height, color);
    }

    @Override
    public void drawRect(int x, int y, int width, int height, ReadableColor color) {
        renderer.drawRect(x + position.getX(), y + position.getY(), width, height, color);
    }

    @Override
    public void drawRect(int x, int y, int width, int height, int topLeftColor, int topRightColor, int bottomLeftColor, int bottomRightColor) {
        renderer.drawRect(x + position.getX(), y + position.getY(), width, height, topLeftColor, topRightColor, bottomLeftColor, bottomRightColor);
    }

    @Override
    public void drawRect(int x, int y, int width, int height, ReadableColor topLeftColor, ReadableColor topRightColor, ReadableColor bottomLeftColor, ReadableColor bottomRightColor) {
        renderer.drawRect(x + position.getX(), y + position.getY(), width, height, topLeftColor, topRightColor, bottomLeftColor, bottomRightColor);
    }

    @Override
    public int drawString(int x, int y, int color, String text) {
        return renderer.drawString(x + position.getX(), y + position.getY(), color, text) - position.getX();
    }

    @Override
    public int drawString(int x, int y, ReadableColor color, String text) {
        return renderer.drawString(x + position.getX(), y + position.getY(), color, text) - position.getX();
    }

    @Override
    public int drawCenteredString(int x, int y, int color, String text) {
        return renderer.drawCenteredString(x + position.getX(), y + position.getY(), color, text) - position.getX();
    }

    @Override
    public int drawCenteredString(int x, int y, ReadableColor color, String text) {
        return renderer.drawCenteredString(x + position.getX(), y + position.getY(), color, text) - position.getX();
    }

    @Override
    public int drawString(int x, int y, int color, String text, boolean shadow) {
        return renderer.drawString(x + position.getX(), y + position.getY(), color, text, shadow) - position.getX();
    }

    @Override
    public int drawString(int x, int y, ReadableColor color, String text, boolean shadow) {
        return renderer.drawString(x + position.getX(), y + position.getY(), color, text, shadow) - position.getX();
    }

    @Override
    public int drawCenteredString(int x, int y, int color, String text, boolean shadow) {
        return renderer.drawCenteredString(x + position.getX(), y + position.getY(), color, text, shadow) - position.getX();
    }

    @Override
    public int drawCenteredString(int x, int y, ReadableColor color, String text, boolean shadow) {
        return renderer.drawCenteredString(x + position.getX(), y + position.getY(), color, text, shadow) - position.getX();
    }

    @Override
    public void invertColors(int right, int bottom, int left, int top) {
        int x = position.getX();
        int y = position.getY();
        renderer.invertColors(right + x, bottom + y, left + x, top + y);
    }
}
