package com.replaymod.render.gui;

import static com.replaymod.render.ReplayModRender.LOGGER;

import java.util.Arrays;
import java.util.function.Consumer;

import com.replaymod.core.ReplayMod;
import com.replaymod.gui.container.GuiPanel;
import com.replaymod.gui.container.GuiScreen;
import com.replaymod.gui.container.GuiVerticalList;
import com.replaymod.gui.element.GuiButton;
import com.replaymod.gui.element.GuiElement;
import com.replaymod.gui.element.GuiLabel;
import com.replaymod.gui.layout.CustomLayout;
import com.replaymod.gui.layout.HorizontalLayout;
import com.replaymod.gui.layout.VerticalLayout;
import com.replaymod.render.FFmpegWriter;
import com.replaymod.render.RenderSettings;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;

public class GuiExportFailed extends GuiScreen {
    public static GuiExportFailed tryToRecover(FFmpegWriter.FFmpegStartupException e, Consumer<RenderSettings> doRestart) {
        // Always log the error first
        LOGGER.error("Rendering video:", e);

        RenderSettings settings = e.getSettings();
        // Check whether the user has configured some custom ffmpeg arguments
        if (settings.getEncodingPreset().getValue().equals(settings.getExportArguments())) {
            // If they haven't, then this is probably a faulty ffmpeg installation and there's nothing we can do
            CrashReport crashReport = CrashReport.forThrowable(e, "Exporting video");
            CrashReportCategory details = crashReport.addCategory("Export details");
            details.setDetail("Settings", settings::toString);
            details.setDetail("FFmpeg log", e::getLog);
            throw new ReportedException(crashReport);
        } else {
            // If they have, ask them whether it was intentional
            GuiExportFailed gui = new GuiExportFailed(e, doRestart);
            gui.display();
            return gui;
        }
    }

    private final GuiLabel logLabel = new GuiLabel(this)
            .setI18nText("replaymod.gui.rendering.error.ffmpeglog");
    private final GuiVerticalList logList = new GuiVerticalList(this).setDrawShadow(true);
    private final GuiButton resetButton = new GuiButton().setI18nLabel("gui.yes").setSize(100, 20);
    private final GuiButton abortButton = new GuiButton().setI18nLabel("gui.no").setSize(100, 20);
    private final GuiPanel info = new GuiPanel(this)
            .setLayout(new VerticalLayout().setSpacing(4))
            .addElements(new VerticalLayout.Data(0.5),
                    new GuiLabel().setI18nText("replaymod.gui.rendering.error.ffmpegargs.1"),
                    new GuiLabel().setI18nText("replaymod.gui.rendering.error.ffmpegargs.2"),
                    new GuiLabel(),
                    new GuiPanel().setLayout(new HorizontalLayout(HorizontalLayout.Alignment.CENTER).setSpacing(5))
                            .addElements(null, resetButton, abortButton)
            );

    {
        setLayout(new CustomLayout<GuiScreen>() {
            @Override
            protected void layout(GuiScreen container, int width, int height) {
                pos(info, width/2 - width(info)/2, (height/2 - height(info) - 30) / 2 + 30);
                pos(logLabel, width/2 - width(logLabel)/2, height/2 + 4);
                pos(logList, 10, y(logLabel) + height(logLabel) + 4);
                size(logList, width - 10 - x(logList), height - 10 - y(logList));
            }
        });

        setTitle(new GuiLabel().setI18nText("replaymod.gui.rendering.error.title"));
        setBackground(Background.DIRT);
    }

    public GuiExportFailed(FFmpegWriter.FFmpegStartupException e, Consumer<RenderSettings> doRestart) {
        logList.getListPanel().addElements(null,
                Arrays.stream(e.getLog().replace("\t", "    ").split("\n"))
                        .map(l -> new GuiLabel().setText(l))
                        .toArray(GuiElement[]::new));

        resetButton.onClick(() -> ReplayMod.instance.runLater(() -> {
            RenderSettings oldSettings = e.getSettings();
            doRestart.accept(new RenderSettings(
                    oldSettings.getRenderMethod(),
                    oldSettings.getEncodingPreset(),
                    oldSettings.getVideoWidth(),
                    oldSettings.getVideoHeight(),
                    oldSettings.getFramesPerSecond(),
                    oldSettings.getBitRate(),
                    oldSettings.getOutputFile(),
                    oldSettings.isRenderNameTags(),
                    oldSettings.isIncludeAlphaChannel(),
                    oldSettings.isStabilizeYaw(),
                    oldSettings.isStabilizePitch(),
                    oldSettings.isStabilizeRoll(),
                    oldSettings.getChromaKeyingColor(),
                    oldSettings.getSphericalFovX(),
                    oldSettings.getSphericalFovY(),
                    oldSettings.isInjectSphericalMetadata(),
                    oldSettings.isDepthMap(),
                    oldSettings.isCameraPathExport(),
                    oldSettings.getAntiAliasing(),
                    oldSettings.getExportCommand(),
                    oldSettings.getEncodingPreset().getValue(),
                    oldSettings.isHighPerformance()
            ));
        }));

        abortButton.onClick(() -> {
            // Assume they know what they're doing
            getMinecraft().setScreen(null);
        });
    }
}