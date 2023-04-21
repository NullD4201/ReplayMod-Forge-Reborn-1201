package com.replaymod.pathing.player;

import com.google.common.util.concurrent.ListenableFuture;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replaystudio.pathing.path.Timeline;

/**
 * Timeline player using the system time.
 */
public class RealtimeTimelinePlayer extends AbstractTimelinePlayer {
    /**
     * Wether the next frame is the first frame.
     * We only start measuring time from the second frame
     * as the first might have to jump in time which might take time.
     */
    private boolean firstFrame;
    private boolean secondFrame;

    /**
     * System time in milliseconds at the start.
     */
    private long startTime;

    public RealtimeTimelinePlayer(ReplayHandler replayHandler) {
        super(replayHandler);
    }

    @Override
    public ListenableFuture<Void> start(Timeline timeline) {
        firstFrame = true;
        return super.start(timeline);
    }

    @Override
    public void onTick() {
        if (secondFrame) {
            secondFrame = false;
            startTime = System.currentTimeMillis();
        }
        super.onTick();
        if (firstFrame) {
            firstFrame = false;
            secondFrame = true;
        }
    }

    @Override
    public long getTimePassed() {
        return startOffset + (firstFrame ? 0 : System.currentTimeMillis() - startTime);
    }
}
