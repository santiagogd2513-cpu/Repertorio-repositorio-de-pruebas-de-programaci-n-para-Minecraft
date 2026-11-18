# BD Acoustic Engine

Independent acoustic-engine addon for Minecraft Java 1.20.1 / Forge 47.4.5 / Java 17. It is designed for a later integration with Beautiful Decor. This repository neither modifies nor bundles the original Beautiful Decor mod.

## Phase 1 implementation

- Forge bootstrap, client config and a reusable API for other mods to submit sound position, velocity and environmental conditions.
- Client integration for positional SoundInstance objects. It applies modelled material/atmospheric gain and listener-motion Doppler pitch while preserving the original sound, coordinates, attenuation mode, stream provider, and loop/tick behavior.
- Moving TickableSoundInstance objects are re-evaluated as their coordinates change. Source velocity is estimated from bounded position history; listener velocity is read from the local player.
- Multi-ray voxel-world probes estimate blocked paths, material transmission, diffraction around obstructions, scattering, reflections and a frequency-dependent low-pass target.
- Six octave-band material tables for absorption, transmission and scattering, with basic block-name classification for common stone, wood, glass, foliage, soil, soft and metal blocks.
- Outdoor atmospheric loss with temperature, humidity and pressure inputs, plus wind projected along the source-listener path. Vanilla hook defaults to standard conditions; adapters may supply local conditions.
- Air/water sound-speed and boundary-impedance estimates, including distinct same-medium and crossing-medium travel-time paths.
- Room RT60 estimation from volume, surface area and average absorption, plus bounded rectangular-room modal resonance frequencies.
- Two-path coherent interference and knife-edge diffraction estimates, exposed through the adapter API.
- Stereo-pan/interaural-delay estimate and an independent aerodynamic-flow noise estimate for adapters. These are estimates, not a full HRTF or fluid simulation.
- Chunk-safe streaming DSP API with stateful low/high-pass filters, smoothed gain, bounded delay and feedback reverb. Stream state is isolated by stream ID and automatically bounded.
- Unit tests for propagation math, material coefficient ranges, hydroacoustic boundaries, spatial pan, airflow, motion tracking, room modes, constructive/destructive interference and chunk-boundary independence.

## Runtime behavior and integration boundary

The standard client hook applies **additional acoustic gain and Doppler pitch** to positional Minecraft sounds. It deliberately leaves Minecraft's native distance rolloff, panning, decoding and final mixer in control. Custom AudioStream providers are delegated to unchanged, and tickable instances remain tickable.

The streaming DSP is exposed through AcousticApi.processStream(...) for an integration that owns decoded PCM blocks. It is **not** injected into every internal vanilla decoder/mixer buffer by this addon. The model returns a travel-time estimate, but the hook does not schedule sounds using that estimate: SoundInstance.getDelay() is preserved because Minecraft uses it for repeat scheduling. The default vanilla integration does not yet synthesize a full room impulse response or separately rendered reflection taps.

All Minecraft block/position distances are treated as metres (one block approximately one metre). Material coefficients, diffraction and transmission values are engineering approximations for game materials, not laboratory measurements. For an air/water crossing without an exact interface location, the travel-time estimator uses an even path split. The stereo helper is a pan approximation rather than a measured HRTF. The room modes and interference utilities are design estimates, not a full wave-field solver. Aerodynamic output is an estimate for a separate airflow source; it does not replace engine/exhaust sound sources.

## Third-party adapter outline

Implement AcousticSource to provide a stable source ID, position, velocity and environmental values. Use AcousticApi.evaluate(...) for propagation, AcousticApi.estimateRoom(...) for room data, AcousticApi.estimateRoomModes(...) for modal resonance, AcousticApi.estimateTwoPathInterference(...) for coherent paths, AcousticApi.estimateStereo(...) for a stereo estimate, and AcousticApi.estimateAirflow(...) for airflow. For PCM streaming, reuse the same UUID for every consecutive chunk passed to AcousticApi.processStream(...) and call AcousticApi.closeStream(...) when the stream ends.

The original Beautiful Decor source stays untouched. Its maintainers can later add a small adapter that calls this API where it owns sound-source positions or decoded stream data.

## Build and verification

Requirements: JDK 17 and Gradle 8.1+.

    gradle --no-daemon clean build

The GitHub Actions workflow builds on pushes, pull requests and manual dispatch. It runs the Gradle build task (including tests) and uploads the source plus compiled JAR artifact. It does not publish a GitHub Release.
