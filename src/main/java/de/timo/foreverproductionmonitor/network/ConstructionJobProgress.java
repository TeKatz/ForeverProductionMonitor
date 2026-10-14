package de.timo.foreverproductionmonitor.network;

/** Completion observation also works when an addon exposes no native AE2 CPU/link. */
final class ConstructionJobProgress {
    enum State { WAITING, READY, CANCELLED, STALLED }
    private static final long MAX_UNOBSERVABLE_TICKS = 20L * 60L * 10L;
    private final long required;
    private long lastProgressTick;
    private long greatestAvailable;

    ConstructionJobProgress(long required, long available, long tick) {
        this.required = required;
        this.greatestAvailable = available;
        this.lastProgressTick = tick;
    }

    State observe(long available, boolean running, boolean done, boolean cancelled, long tick) {
        if (cancelled) return State.CANCELLED;
        if (done || available >= required) return State.READY;
        if (running || available > greatestAvailable) lastProgressTick = tick;
        greatestAvailable = Math.max(greatestAvailable, available);
        return tick - lastProgressTick >= MAX_UNOBSERVABLE_TICKS ? State.STALLED : State.WAITING;
    }
}
