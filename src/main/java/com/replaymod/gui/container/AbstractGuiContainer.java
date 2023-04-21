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
package com.replaymod.gui.container;

import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.replaymod.gui.GuiRenderer;
import com.replaymod.gui.OffsetGuiRenderer;
import com.replaymod.gui.RenderInfo;
import com.replaymod.gui.element.AbstractComposedGuiElement;
import com.replaymod.gui.element.ComposedGuiElement;
import com.replaymod.gui.element.GuiElement;
import com.replaymod.gui.layout.HorizontalLayout;
import com.replaymod.gui.layout.Layout;
import com.replaymod.gui.layout.LayoutData;
import com.replaymod.gui.versions.MCVer;

import de.johni0702.minecraft.gui.utils.lwjgl.Point;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;

public abstract class AbstractGuiContainer<T extends AbstractGuiContainer<T>>
        extends AbstractComposedGuiElement<T> implements com.replaymod.gui.container.GuiContainer<T> {

    private static final Layout DEFAULT_LAYOUT = new HorizontalLayout();

    private Map<com.replaymod.gui.element.GuiElement, com.replaymod.gui.layout.LayoutData> elements = new LinkedHashMap<>();

    private Map<com.replaymod.gui.element.GuiElement, Pair<ReadablePoint, ReadableDimension>> layedOutElements;

    private Layout layout = DEFAULT_LAYOUT;

    private ReadableColor backgroundColor;

    public AbstractGuiContainer() {
    }

    public AbstractGuiContainer(com.replaymod.gui.container.GuiContainer container) {
        super(container);
    }

    @Override
    public T setLayout(Layout layout) {
        this.layout = layout;
        return getThis();
    }

    @Override
    public Layout getLayout() {
        return layout;
    }

    @Override
    public void convertFor(com.replaymod.gui.element.GuiElement element, Point point) {
        convertFor(element, point, element.getLayer());
    }

    @Override
    public void convertFor(com.replaymod.gui.element.GuiElement element, Point point, int relativeLayer) {
        if (layedOutElements == null || !layedOutElements.containsKey(element)) {
            layout(null, new RenderInfo(0, 0, 0, relativeLayer));
        }
        checkState(layedOutElements != null, "Cannot convert position unless rendered at least once.");
        Pair<ReadablePoint, ReadableDimension> pair = layedOutElements.get(element);
        checkState(pair != null, "Element " + element + " not part of " + this);
        ReadablePoint pos = pair.getKey();
        if (getContainer() != null) {
            getContainer().convertFor(this, point, relativeLayer + getLayer());
        }
        point.translate(-pos.getX(), -pos.getY());
    }

    @Override
    public Collection<com.replaymod.gui.element.GuiElement> getChildren() {
        return Collections.unmodifiableCollection(elements.keySet());
    }

    @Override
    public Map<com.replaymod.gui.element.GuiElement, com.replaymod.gui.layout.LayoutData> getElements() {
        return Collections.unmodifiableMap(elements);
    }

    @Override
    public T addElements(com.replaymod.gui.layout.LayoutData layoutData, com.replaymod.gui.element.GuiElement... elements) {
        if (layoutData == null) {
            layoutData = LayoutData.NONE;
        }
        for (com.replaymod.gui.element.GuiElement element : elements) {
            this.elements.put(element, layoutData);
            element.setContainer(this);
        }
        return getThis();
    }

    @Override
    public T removeElement(com.replaymod.gui.element.GuiElement element) {
        if (elements.remove(element) != null) {
            element.setContainer(null);
            if (layedOutElements != null) {
                layedOutElements.remove(element);
            }
        }
        return getThis();
    }

    @Override
    public void layout(ReadableDimension size, RenderInfo renderInfo) {
        super.layout(size, renderInfo);
        if (size == null) return;
        try {
            layedOutElements = layout.layOut(this, size);
        } catch (Exception ex) {
            CrashReport crashReport = CrashReport.forThrowable(ex, "Gui Layout");
            renderInfo.addTo(crashReport);
            CrashReportCategory category = crashReport.addCategory("Gui container details");
            MCVer.addDetail(category, "Container", this::toString);
            MCVer.addDetail(category, "Layout", layout::toString);
            throw new ReportedException(crashReport);
        }
        for (final Map.Entry<com.replaymod.gui.element.GuiElement, Pair<ReadablePoint, ReadableDimension>> e : layedOutElements.entrySet()) {
            com.replaymod.gui.element.GuiElement element = e.getKey();
            if (element instanceof com.replaymod.gui.element.ComposedGuiElement) {
                if (((com.replaymod.gui.element.ComposedGuiElement) element).getMaxLayer() < renderInfo.layer) {
                    continue;
                }
            } else {
                if (element.getLayer() != renderInfo.layer) {
                    continue;
                }
            }
            ReadablePoint ePosition = e.getValue().getLeft();
            ReadableDimension eSize = e.getValue().getRight();
            element.layout(eSize, renderInfo.offsetMouse(ePosition.getX(), ePosition.getY())
                    .layer(renderInfo.getLayer() - element.getLayer()));
        }
    }

    @Override
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        super.draw(renderer, size, renderInfo);
        if (backgroundColor != null && renderInfo.getLayer() == 0) {
            renderer.drawRect(0, 0, size.getWidth(), size.getHeight(), backgroundColor);
        }
        for (final Map.Entry<com.replaymod.gui.element.GuiElement, Pair<ReadablePoint, ReadableDimension>> e : layedOutElements.entrySet()) {
            com.replaymod.gui.element.GuiElement element = e.getKey();
            boolean strict;
            if (element instanceof com.replaymod.gui.element.ComposedGuiElement) {
                if (((ComposedGuiElement) element).getMaxLayer() < renderInfo.layer) {
                    continue;
                }
                strict = renderInfo.layer == 0;
            } else {
                if (element.getLayer() != renderInfo.layer) {
                    continue;
                }
                strict = true;
            }
            final ReadablePoint ePosition = e.getValue().getLeft();
            final ReadableDimension eSize = e.getValue().getRight();
            try {
                OffsetGuiRenderer eRenderer = new OffsetGuiRenderer(renderer, ePosition, eSize, strict);
                eRenderer.startUsing();
                e.getKey().draw(eRenderer, eSize, renderInfo.offsetMouse(ePosition.getX(), ePosition.getY())
                        .layer(renderInfo.getLayer() - e.getKey().getLayer()));
                eRenderer.stopUsing();
            } catch (Exception ex) {
                CrashReport crashReport = CrashReport.forThrowable(ex, "Rendering Gui");
                renderInfo.addTo(crashReport);
                CrashReportCategory category = crashReport.addCategory("Gui container details");
                MCVer.addDetail(category, "Container", this::toString);
                MCVer.addDetail(category, "Width", () -> "" + size.getWidth());
                MCVer.addDetail(category, "Height", () -> "" + size.getHeight());
                MCVer.addDetail(category, "Layout", layout::toString);
                category = crashReport.addCategory("Gui element details");
                MCVer.addDetail(category, "Element", () -> e.getKey().toString());
                MCVer.addDetail(category, "Position", ePosition::toString);
                MCVer.addDetail(category, "Size", eSize::toString);
                if (e.getKey() instanceof com.replaymod.gui.container.GuiContainer) {
                    MCVer.addDetail(category, "Layout", () -> ((GuiContainer) e.getKey()).getLayout().toString());
                }
                throw new ReportedException(crashReport);
            }
        }
    }

    @Override
    public ReadableDimension calcMinSize() {
        return layout.calcMinSize(this);
    }

    @Override
    public T sortElements() {
        sortElements(new Comparator<com.replaymod.gui.element.GuiElement>() {
            @SuppressWarnings("unchecked")
            @Override
            public int compare(com.replaymod.gui.element.GuiElement o1, com.replaymod.gui.element.GuiElement o2) {
                if (o1 instanceof Comparable && o2 instanceof Comparable) {
                    return ((Comparable) o1).compareTo(o2);
                }
                return o1.hashCode() - o2.hashCode();
            }
        });
        return getThis();
    }

    @Override
    public T sortElements(final Comparator<GuiElement> comparator) {
        elements = elements.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey(comparator))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
        return getThis();
    }

    @Override
    public ReadableColor getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public T setBackgroundColor(ReadableColor backgroundColor) {
        this.backgroundColor = backgroundColor;
        return getThis();
    }
}
