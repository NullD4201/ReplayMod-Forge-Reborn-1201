package com.replaymod.core.tweaker;

/*
import com.replaymod.extras.modcore.ModCoreInstaller;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.launch.GlobalProperties;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.launch.platform.MixinPlatformManager;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;

// Fabric equivalent is in ReplayModMMLauncher
public class ReplayModTweaker implements ITweaker {

    private static final String MIXIN_TWEAKER = "org.spongepowered.asm.launch.MixinTweaker";

    public ReplayModTweaker() {
        injectMixinTweaker();
    }

    private void injectMixinTweaker() {
        @SuppressWarnings("unchecked")
        List<String> tweakClasses = (List<String>) Launch.blackboard.get("TweakClasses");

        // If the MixinTweaker is already queued (because of anothe r mod), then there's nothing we need to to
        if (tweakClasses.contains(MIXIN_TWEAKER)) {
            return;
        }

        // If it is already booted, we're also good to go
        if (GlobalProperties.get(GlobalProperties.Keys.INIT) != null) {
            return;
        }

        System.out.println("Injecting MixinTweaker from ReplayModTweaker");

        // Otherwise, we need to take things into our own hands because the normal way to chainload a tweaker
        // (by adding it to the TweakClasses list during injectIntoClassLoader) is too late for Mixin.
        // Instead we instantiate the MixinTweaker on our own and add it to the current Tweaks list immediately.
        Launch.classLoader.addClassLoaderExclusion(MIXIN_TWEAKER.substring(0, MIXIN_TWEAKER.lastIndexOf('.')));
        @SuppressWarnings("unchecked")
        List<ITweaker> tweaks = (List<ITweaker>) Launch.blackboard.get("Tweaks");
        try {
            tweaks.add((ITweaker) Class.forName(MIXIN_TWEAKER, true, Launch.classLoader).newInstance());
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        // Add our jar as a Mixin container (cause normally Mixin detects those via the TweakClass manifest entry)
        URI uri;
        try {
            uri = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        MixinPlatformManager platform = MixinBootstrap.getPlatform();
                platform.addContainer(new ContainerHandleURI(uri));
                        
                initModCore("1.12.2");
                                    }

    @Override
    public String getLaunchTarget() {
        return null;
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }

    private void initModCore(String mcVer) {
        try {
            if (System.getProperty("REPLAYMOD_SKIP_MODCORE", "false").equalsIgnoreCase("true")) {
                System.out.println("ReplayMod not initializing ModCore because REPLAYMOD_SKIP_MODCORE is true.");
                return;
            }

            if ((Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment")) {
                System.out.println("ReplayMod not initializing ModCore because we're in a development environment.");
                return;
            }

            int result = ModCoreInstaller.initialize(Launch.minecraftHome, mcVer + "_forge");
            if (result != -2) { // Don't even bother logging the result if there's no ModCore for this version.
                System.out.println("ReplayMod ModCore init result: " + result);
            }
            if (ModCoreInstaller.isErrored()) {
                System.err.println(ModCoreInstaller.getError());
            }
        } catch (Throwable t) {
            System.err.println("ReplayMod caught error during ModCore init:");
            t.printStackTrace();
        }
    }
}
*/
