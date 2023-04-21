package com.replaymod.extras;

import com.replaymod.core.ReplayMod;
import com.replaymod.core.versions.MCVer.Keyboard;
import com.replaymod.gui.element.GuiImage;
import com.replaymod.gui.layout.HorizontalLayout;
import com.replaymod.gui.utils.EventRegistrations;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.events.ReplayOpenedCallback;
import com.replaymod.replay.gui.overlay.GuiReplayOverlay;

public class QuickMode extends EventRegistrations implements Extra {
    private ReplayModReplay module;

    private final GuiImage indicator = new GuiImage().setTexture(ReplayMod.TEXTURE, 40, 100, 16, 16).setSize(16, 16);

    @Override
    public void register(final ReplayMod mod) {
        this.module = ReplayModReplay.instance;

        mod.getKeyBindingRegistry().registerKeyMapping("replaymod.input.quickmode", Keyboard.KEY_Q, () -> {
            ReplayHandler replayHandler = module.getReplayHandler();
            if (replayHandler == null) {
                return;
            }
            replayHandler.getReplaySender().setSyncModeAndWait();
            mod.runLater(() -> {
                replayHandler.ensureQuickModeInitialized(() -> {
                    boolean enabled = !replayHandler.isQuickMode();
                    updateIndicator(replayHandler.getOverlay(), enabled);
                    replayHandler.setQuickMode(enabled);
                    replayHandler.getReplaySender().setAsyncMode(true);
                });
            });
        }, true);

        register();
    }

    {
        on(ReplayOpenedCallback.EVENT, replayHandler -> updateIndicator(replayHandler.getOverlay(), replayHandler.isQuickMode()));
    }

    private void updateIndicator(GuiReplayOverlay overlay, boolean enabled) {
        if (enabled) {
            overlay.statusIndicatorPanel.addElements(new HorizontalLayout.Data(1), indicator);
        } else {
            overlay.statusIndicatorPanel.removeElement(indicator);
        }
    }
}
