package com.replaymod.mixin;

import static com.replaymod.core.versions.MCVer.getMinecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;
import com.replaymod.render.capturer.CubicOpenGlFrameCapturer;
import com.replaymod.render.hooks.EntityRendererHandler;

@Mixin(value = net.minecraft.client.renderer.GameRenderer.class)
public abstract class Mixin_Omnidirectional_Rotation {
    private EntityRendererHandler getHandler() {
        return ((EntityRendererHandler.IEntityRenderer) getMinecraft().gameRenderer).replayModRender_getHandler();
    }

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void replayModRender_setupCubicFrameRotation(
            float partialTicks,
            long frameStartNano,
            PoseStack matrixStack,
            CallbackInfo ci
    ) {
        if (getHandler() != null && getHandler().data instanceof CubicOpenGlFrameCapturer.Data data) {
            float angle = 0;
            float x = 0;
            float y = 0;
            switch (data) {
                case FRONT -> {
                    angle = 0;
                    y = 1;
                }
                case RIGHT -> {
                    angle = 90;
                    y = 1;
                }
                case BACK -> {
                    angle = 180;
                    y = 1;
                }
                case LEFT -> {
                    angle = -90;
                    y = 1;
                }
                case TOP -> {
                    angle = -90;
                    x = 1;
                }
                case BOTTOM -> {
                    angle = 90;
                    x = 1;
                }
            }
            matrixStack.mulPose(new Vector3f().rotateAxis(angle, x, y, 0));
        }
    }
}
