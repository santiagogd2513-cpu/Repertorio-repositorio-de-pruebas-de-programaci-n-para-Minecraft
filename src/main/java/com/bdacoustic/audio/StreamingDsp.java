package com.bdacoustic.audio;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Stateful, chunk-safe one-pole filter core. Call process for consecutive samples of one stream. */
public final class StreamingDsp {
    private static final int MAX_STREAMS = 256;
    private final Map<UUID, State> states = new LinkedHashMap<>(32, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<UUID, State> eldest) { return size() > MAX_STREAMS; }
    };
    private static final class State { double low = 0; }
    public synchronized void process(UUID streamId, float[] samples, double cutoffHz, double sampleRate) {
        if (samples == null || streamId == null || samples.length == 0) return;
        double sr = Math.max(8000, sampleRate);
        double cutoff = Math.max(20, Math.min(sr * 0.45, cutoffHz));
        double alpha = 1.0 - Math.exp(-2.0 * Math.PI * cutoff / sr);
        State state = states.computeIfAbsent(streamId, ignored -> new State());
        for (int i = 0; i < samples.length; i++) {
            state.low += alpha * (samples[i] - state.low);
            samples[i] = (float) state.low;
        }
    }
    public synchronized void forget(UUID streamId) { states.remove(streamId); }
    public synchronized int activeStreams() { return states.size(); }
}