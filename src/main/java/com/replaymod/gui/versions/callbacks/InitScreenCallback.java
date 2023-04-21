package com.replaymod.gui.versions.callbacks;

import java.util.Collection;

import com.replaymod.gui.utils.Event;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

public interface InitScreenCallback {
    Event<InitScreenCallback> EVENT = Event.create((listeners) ->
            (screen, buttons) -> {
                for (InitScreenCallback listener : listeners) {
                    listener.initScreen(screen, buttons);
                }
            }
    );

    void initScreen(Screen screen, Collection<AbstractWidget> buttons);

    interface Pre {
        Event<InitScreenCallback.Pre> EVENT = Event.create((listeners) ->
                (screen) -> {
                    for (InitScreenCallback.Pre listener : listeners) {
                        listener.preInitScreen(screen);
                    }
                }
        );

        void preInitScreen(Screen screen);
    }
}
