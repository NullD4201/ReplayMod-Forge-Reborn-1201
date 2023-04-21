package com.replaymod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.replaymod.render.hooks.EntityRendererHandler;

import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public abstract class Mixin_Omnidirectional_Camera implements EntityRendererHandler.IEntityRenderer {
    
    private static final String METHOD = "getProjectionMatrix";
    private static final String TARGET = "Lcom/mojang/math/Matrix4f;perspective(DFFF)Lcom/mojang/math/Matrix4f;";
    private static final boolean TARGET_REMAP = true;
    private static final float OMNIDIRECTIONAL_FOV = 90;

    @ModifyArg(method = METHOD, at = @At(value = "INVOKE", target = TARGET, remap = TARGET_REMAP), index = 0)

    private double replayModRender_perspective_fov(double fovY) {
        return isOmnidirectional() ? OMNIDIRECTIONAL_FOV : fovY;
    }

    @ModifyArg(method = METHOD, at = @At(value = "INVOKE", target = TARGET, remap = TARGET_REMAP), index = 1)
    private float replayModRender_perspective_aspect(float aspect) {
        return isOmnidirectional() ? 1 : aspect;
    }

    @Unique
    private boolean isOmnidirectional() {
        return replayModRender_getHandler() != null && replayModRender_getHandler().omnidirectional;
    }
}
