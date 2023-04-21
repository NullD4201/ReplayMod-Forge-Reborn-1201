package com.replaymod.replay;

import org.lwjgl.glfw.GLFW;

import com.replaymod.core.ReplayMod;
import com.replaymod.core.utils.WrappedTimer;
import com.replaymod.core.versions.MCVer;
import com.replaymod.replay.camera.CameraController;
import com.replaymod.replay.camera.CameraEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;


public class InputReplayTimer extends WrappedTimer {
    private final ReplayModReplay mod;
    private final Minecraft mc;

    public InputReplayTimer(Timer wrapped, ReplayModReplay mod) {
        super(wrapped);
        this.mod = mod;
        this.mc = mod.getCore().getMinecraft();
    }

    @Override
    public int
    advanceTime(
            long sysClock
    ) {
        int ticksThisFrame =
                super.advanceTime(
                        sysClock
                );

        ReplayMod.instance.runTasks();


        // If we are in a replay, we have to manually process key and mouse events as the
        // tick speed may vary or there may not be any ticks at all (when the replay is paused)
        if (mod.getReplayHandler() != null && mc.level != null && mc.player != null) {
            if (mc.screen == null || mc.screen.passEvents) {
                GLFW.glfwPollEvents();
                MCVer.processKeyBinds();
            }
            mc.keyboardHandler.tick();
        }
        return ticksThisFrame;
    }

    public static void handleScroll(int wheel) {
        if (wheel != 0) {
            ReplayHandler replayHandler = ReplayModReplay.instance.getReplayHandler();
            if (replayHandler != null) {
                CameraEntity cameraEntity = replayHandler.getCameraEntity();
                if (cameraEntity != null) {
                    CameraController controller = cameraEntity.getCameraController();
                    while (wheel > 0) {
                        controller.increaseSpeed();
                        wheel--;
                    }
                    while (wheel < 0) {
                        controller.decreaseSpeed();
                        wheel++;
                    }
                }
            }
        }
    }
}
