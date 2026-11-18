package com.bdacoustic;

import com.bdacoustic.audio.StreamingDsp;
import com.bdacoustic.physics.AcousticMath;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class AcousticCoreTest {
    @Test void travelTimeIsDistanceOverSpeed() {
        assertEquals(1.0, AcousticMath.travelTime(343.2, 343.2), 1.0e-9);
    }
    @Test void soundSpeedIncreasesWithTemperature() {
        assertTrue(AcousticMath.speedOfSound(30, 50) > AcousticMath.speedOfSound(0, 50));
    }
    @Test void dopplerRatioRemainsBounded() {
        assertTrue(AcousticMath.dopplerRatio(500, 0, 343.2) <= 2.0);
        assertTrue(AcousticMath.dopplerRatio(-500, 0, 343.2) >= 0.5);
    }
    @Test void streamingFilterRetainsStateBetweenChunks() {
        StreamingDsp dsp = new StreamingDsp();
        UUID id = UUID.randomUUID();
        float[] first = new float[] { 1f, 1f, 1f };
        float[] second = new float[] { 0f, 0f, 0f };
        dsp.process(id, first, 1000, 48000);
        dsp.process(id, second, 1000, 48000);
        assertTrue(first[2] > 0);
        assertTrue(second[0] > 0);
        assertEquals(1, dsp.activeStreams());
        dsp.forget(id);
        assertEquals(0, dsp.activeStreams());
    }
}