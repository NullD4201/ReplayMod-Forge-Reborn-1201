package com.replaymod.recording;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.replaymod.core.KeyBindingRegistry;
import com.replaymod.core.Module;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.utils.Restrictions;
import com.replaymod.core.versions.MCVer.Keyboard;
import com.replaymod.mixin.NetworkManagerAccessor;
import com.replaymod.recording.handler.ConnectionEventHandler;
import com.replaymod.recording.handler.GuiHandler;
import com.replaymod.recording.packet.PacketListener;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import net.minecraft.network.Connection;
import net.minecraftforge.network.NetworkRegistry;


public class ReplayModRecording implements Module {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final AttributeKey<Void> ATTR_CHECKED = AttributeKey.newInstance("ReplayModRecording_checked");

    {
        instance = this;
    }

    public static ReplayModRecording instance;

    private ReplayMod core;

    private ConnectionEventHandler connectionEventHandler;

    public ReplayModRecording(ReplayMod mod) {
        core = mod;

        core.getSettingsRegistry().register(Setting.class);
    }

    @Override
    public void registerKeyMappings(KeyBindingRegistry registry) {
        registry.registerKeyMapping("replaymod.input.marker", Keyboard.KEY_M, new Runnable() {
            @Override
            public void run() {
                PacketListener packetListener = connectionEventHandler.getPacketListener();
                if (packetListener != null) {
                    packetListener.addMarker(null);
                    core.printInfoToChat("replaymod.chat.addedmarker");
                }
            }
        }, false);
    }

    @Override
    public void initClient() {
        connectionEventHandler = new ConnectionEventHandler(LOGGER, core);

        new GuiHandler(core).register();

        NetworkRegistry.newEventChannel(Restrictions.PLUGIN_CHANNEL, () -> "0", any -> true, any -> true);
    }


    public void initiateRecording(Connection networkManager)
    {
    	Channel channel = ((NetworkManagerAccessor)networkManager).getChannel();
    	if (channel.pipeline().get("ReplayModReplay_replaySender") != null)
    		return; 
        if (channel.hasAttr(ATTR_CHECKED))
        	return; 
        channel.attr(ATTR_CHECKED).set(null);
        this.connectionEventHandler.onConnectedToServerEvent(networkManager);
    }

    public ConnectionEventHandler getConnectionEventHandler() {
        return connectionEventHandler;
    }
}
