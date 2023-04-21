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
package com.replaymod.gui.element;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

import com.mojang.blaze3d.systems.RenderSystem;
import com.replaymod.gui.GuiRenderer;
import com.replaymod.gui.RenderInfo;
import com.replaymod.gui.container.GuiContainer;
import com.replaymod.gui.function.Clickable;

import de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import de.johni0702.minecraft.gui.utils.lwjgl.Point;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import de.johni0702.minecraft.gui.utils.lwjgl.WritableDimension;
import de.johni0702.minecraft.gui.utils.lwjgl.WritablePoint;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public abstract class AbstractGuiTexturedButton<T extends AbstractGuiTexturedButton<T>> extends AbstractGuiClickable<T> implements Clickable, IGuiTexturedButton<T> {
    private ResourceLocation texture;

    private SoundEvent sound = SoundEvents.UI_BUTTON_CLICK;

    private ReadableDimension textureSize = new ReadableDimension() {
        @Override
        public int getWidth() {
            return getMaxSize().getWidth();
        }

        @Override
        public int getHeight() {
            return getMaxSize().getHeight();
        }

        @Override
        public void getSize(WritableDimension dest) {
            getMaxSize().getSize(dest);
        }
    };

    private ReadableDimension textureTotalSize;

    private ReadablePoint textureNormal;

    private ReadablePoint textureHover;

    private ReadablePoint textureDisabled;

    public AbstractGuiTexturedButton() {
    }

    public AbstractGuiTexturedButton(GuiContainer container) {
        super(container);
    }

    @Override
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        super.draw(renderer, size, renderInfo);

        renderer.bindTexture(texture);

        ReadablePoint texture = textureNormal;
        if (!isEnabled()) {
            texture = textureDisabled;
        } else if (isMouseHovering(new Point(renderInfo.mouseX, renderInfo.mouseY))) {
            texture = textureHover;
        }

        if (texture == null) { // Button is disabled but we have no texture for that
            RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1);
            texture = textureNormal;
        }

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.blendFunc(770, 771);

        renderer.drawTexturedRect(0, 0, texture.getX(), texture.getY(), size.getWidth(), size.getHeight(),
                textureSize.getWidth(), textureSize.getHeight(),
                textureTotalSize.getWidth(), textureTotalSize.getHeight());
    }

    @Override
    public ReadableDimension calcMinSize() {
        return new Dimension(0, 0);
    }

    @Override
    public void onClick() {
        AbstractGuiButton.playClickSound(getMinecraft(), sound);
        super.onClick();
    }

    @Override
    public T setTexture(ResourceLocation resourceLocation, int size) {
        return setTexture(resourceLocation, size, size);
    }

    @Override
    public T setTexture(ResourceLocation resourceLocation, int width, int height) {
        this.texture = resourceLocation;
        this.textureTotalSize = new Dimension(width, height);
        return getThis();
    }

    @Override
    public T setTextureSize(int size) {
        return setTextureSize(size, size);
    }

    @Override
    public T setTextureSize(int width, int height) {
        this.textureSize = new Dimension(width, height);
        return getThis();
    }

    @Override
    public T setTexturePosH(final int x, final int y) {
        return setTexturePosH(new Point(x, y));
    }

    @Override
    public T setTexturePosV(final int x, final int y) {
        return setTexturePosV(new Point(x, y));
    }

    @Override
    public T setTexturePosH(final ReadablePoint pos) {
        this.textureNormal = pos;
        this.textureHover = new ReadablePoint() {
            @Override
            public int getX() {
                return pos.getX() + textureSize.getWidth();
            }

            @Override
            public int getY() {
                return pos.getY();
            }

            @Override
            public void getLocation(WritablePoint dest) {
                dest.setLocation(getX(), getY());
            }
        };
        return getThis();
    }

    @Override
    public T setTexturePosV(final ReadablePoint pos) {
        this.textureNormal = pos;
        this.textureHover = new ReadablePoint() {
            @Override
            public int getX() {
                return pos.getX();
            }

            @Override
            public int getY() {
                return pos.getY() + textureSize.getHeight();
            }

            @Override
            public void getLocation(WritablePoint dest) {
                dest.setLocation(getX(), getY());
            }
        };
        return getThis();
    }

    @Override
    public T setTexturePos(int normalX, int normalY, int hoverX, int hoverY) {
        return setTexturePos(new Point(normalX, normalY), new Point(hoverX, hoverY));
    }

    @Override
    public T setTexturePos(ReadablePoint normal, ReadablePoint hover) {
        this.textureNormal = normal;
        this.textureHover = hover;
        return getThis();
    }

    @Override
    public T setTexturePos(int normalX, int normalY, int hoverX, int hoverY, int disabledX, int disabledY) {
        return setTexturePos(new Point(normalX, normalY), new Point(hoverX, hoverY), new Point(disabledX, disabledY));
    }

    @Override
    public T setTexturePos(ReadablePoint normal, ReadablePoint hover, ReadablePoint disabled) {
        this.textureDisabled = disabled;
        return setTexturePos(normal, hover);
    }

    @Override
    public T setSound(SoundEvent sound) {
        this.sound = sound;
        return getThis();
    }

    public SoundEvent getSound() {
        return this.sound;
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }

    public ReadableDimension getTextureSize() {
        return this.textureSize;
    }

    public ReadableDimension getTextureTotalSize() {
        return this.textureTotalSize;
    }

    public ReadablePoint getTextureNormal() {
        return this.textureNormal;
    }

    public ReadablePoint getTextureHover() {
        return this.textureHover;
    }

    public ReadablePoint getTextureDisabled() {
        return this.textureDisabled;
    }
}
