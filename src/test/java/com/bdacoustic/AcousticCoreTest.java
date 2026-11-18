package com.bdacoustic;

import com.bdacoustic.audio.AcousticDspParameters;
import com.bdacoustic.audio.StreamingDsp;
import com.bdacoustic.physics.AcousticMath;
import com.bdacoustic.physics.AcousticMaterial;
import com.bdacoustic.physics.AcousticMotionTracker;
import com.bdacoustic.physics.AerodynamicNoiseModel;
import com.bdacoustic.physics.AtmosphericModel;
import com.bdacoustic.physics.Hydroacoustics;
import com.bdacoustic.physics.RoomAcoustics;
import com.bdacoustic.physics.RoomResonanceModel;
import com.bdacoustic.physics.SpatialAudioModel;
import com.bdacoustic.physics.WaveInterferenceModel;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;
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

    @Test void atmosphericAbsorptionNeverAmplifies() {
        double gain = AtmosphericModel.atmosphericGain(1500, 4, 20, 50);
        assertTrue(gain > 0.0 && gain <= 1.0);
    }

    @Test void pressureIsAnIndependentAtmosphericInput() {
        double normal = AtmosphericModel.airAbsorptionDbPerKm(4, 20, 50, 101325);
        double lowPressure = AtmosphericModel.airAbsorptionDbPerKm(4, 20, 50, 80000);
        assertTrue(lowPressure > normal);
    }

    @Test void roomEstimateIsFiniteAndBounded() {
        RoomAcoustics.Estimate estimate = RoomAcoustics.estimate(120, 180, 0.25);
        assertTrue(estimate.rt60Seconds() >= 0.05 && estimate.rt60Seconds() <= 12.0);
        assertTrue(Double.isFinite(estimate.equivalentAbsorptionArea()));
    }

    @Test void roomModesAreSortedAndBounded() {
        List<RoomResonanceModel.Mode> modes = RoomResonanceModel.estimate(8, 6, 3, 343.2, 0.25, 24);
        assertFalse(modes.isEmpty());
        assertTrue(modes.size() <= 24);
        for (int i = 1; i < modes.size(); i++) {
            assertTrue(modes.get(i).frequencyHz() >= modes.get(i - 1).frequencyHz());
        }
        assertTrue(modes.get(0).frequencyHz() > 0.0);
    }

    @Test void waveInterferenceHasConstructiveAndDestructiveCases() {
        double wavelength = 343.2 / 1000.0;
        WaveInterferenceModel.Interference constructive =
                WaveInterferenceModel.twoPath(5.0, 5.0 + wavelength, 1000.0, 343.2);
        WaveInterferenceModel.Interference destructive =
                WaveInterferenceModel.twoPath(5.0, 5.0 + wavelength * 0.5, 1000.0, 343.2);
        assertTrue(constructive.amplitudeGain() > 0.99);
        assertTrue(destructive.amplitudeGain() < 0.001);
    }

    @Test void diffractionDoesNotExceedUnity() {
        assertTrue(WaveInterferenceModel.knifeEdgeGain(1.0) <= 1.0);
        assertTrue(WaveInterferenceModel.knifeEdgeGain(1.0) > 0.0);
        assertEquals(1.0, WaveInterferenceModel.knifeEdgeGain(-1.0), 1.0e-9);
    }

    @Test void hydroacousticBoundaryIsStronglyMismatched() {
        double gain = Hydroacoustics.boundaryAmplitudeGain(true);
        assertTrue(gain > 0.0 && gain < 0.1);
        assertTrue(Hydroacoustics.travelTime(1480, true, true, 343.2) > 0.9);
    }

    @Test void stereoPanMovesToTheCorrectSide() {
        Vec3 listener = Vec3.ZERO;
        Vec3 forward = new Vec3(0, 0, 1);
        SpatialAudioModel.StereoGains right = SpatialAudioModel.estimate(new Vec3(5, 0, 0), listener, forward);
        SpatialAudioModel.StereoGains left = SpatialAudioModel.estimate(new Vec3(-5, 0, 0), listener, forward);
        assertTrue(right.right() > right.left());
        assertTrue(left.left() > left.right());
    }

    @Test void airflowEstimateIncreasesWithSpeed() {
        AerodynamicNoiseModel.Estimate slow = AerodynamicNoiseModel.estimate(10, 2, 0.7, 0.3);
        AerodynamicNoiseModel.Estimate fast = AerodynamicNoiseModel.estimate(80, 2, 0.7, 0.3);
        assertTrue(fast.broadbandGain() > slow.broadbandGain());
        assertTrue(fast.whistleFrequencyHz() > slow.whistleFrequencyHz());
    }

    @Test void materialCoefficientsStayWithinUnitInterval() {
        for (AcousticMaterial material : AcousticMaterial.values()) {
            for (int band = 0; band < 6; band++) {
                assertTrue(material.absorption(band) >= 0 && material.absorption(band) <= 1);
                assertTrue(material.transmission(band) >= 0 && material.transmission(band) <= 1);
                assertTrue(material.scattering(band) >= 0 && material.scattering(band) <= 1);
            }
        }
    }

    @Test void motionTrackerEstimatesMetresPerSecond() {
        AcousticMotionTracker tracker = new AcousticMotionTracker();
        UUID id = UUID.randomUUID();
        Vec3 first = tracker.update(id, Vec3.ZERO, 1_000_000_000L);
        assertEquals(0.0, first.x, 1.0e-9);
        Vec3 velocity = tracker.update(id, new Vec3(2, 0, 0), 2_000_000_000L);
        assertEquals(2.0, velocity.x, 1.0e-9);
        assertEquals(1, tracker.trackedSources());
        tracker.forget(id);
        assertEquals(0, tracker.trackedSources());
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

    @Test void streamingOutputIsIndependentOfChunkBoundaries() {
        float[] input = new float[2048];
        for (int i = 0; i < input.length; i++) input[i] = (float) Math.sin(i * 0.07) * 0.7f;
        float[] whole = input.clone();
        float[] chunked = input.clone();
        AcousticDspParameters settings = new AcousticDspParameters(0.85, 3200, 75, 0.012, 0.25);
        StreamingDsp dsp = new StreamingDsp();
        UUID wholeId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID chunkId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        dsp.process(wholeId, whole, settings, 48000);
        processRange(dsp, chunkId, chunked, 0, 311, settings);
        processRange(dsp, chunkId, chunked, 311, 907, settings);
        processRange(dsp, chunkId, chunked, 907, chunked.length, settings);
        assertArrayEquals(whole, chunked, 1.0e-6f);
    }

    @Test void streamingStateIsBounded() {
        StreamingDsp dsp = new StreamingDsp();
        for (int i = 0; i < 140; i++) {
            dsp.process(UUID.randomUUID(), new float[]{0.1f}, 2000, 48000);
        }
        assertTrue(dsp.activeStreams() <= 128);
    }

    private static void processRange(StreamingDsp dsp, UUID id, float[] target,
                                     int start, int end, AcousticDspParameters settings) {
        float[] block = new float[end - start];
        System.arraycopy(target, start, block, 0, block.length);
        dsp.process(id, block, settings, 48000);
        System.arraycopy(block, 0, target, start, block.length);
    }
}
