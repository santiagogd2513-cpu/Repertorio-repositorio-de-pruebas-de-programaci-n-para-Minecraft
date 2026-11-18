# BD Acoustic Engine

Independent acoustic-engine addon for Minecraft Java 1.20.1 / Forge 47.4.5 / Java 17. The original Beautiful Decor source is not modified or bundled.

## Audio routing integration update

- Every sound submitted through Forge's client `PlaySoundEvent` is decorated, including sounds with `Attenuation.NONE`. Relative sounds keep neutral world-propagation values rather than being incorrectly traced from camera-relative coordinates.
- World-positioned sources use the voxel propagation model for additional gain and Doppler pitch. `Attenuation.NONE` sources can still receive wall-transmission gain; this is separate from vanilla distance rolloff.
- A client Mixin now hooks Minecraft's actual `com.mojang.blaze3d.audio.Channel.play()` method. This is below the event/decorator layer, at the real OpenAL source, so static-buffer and streaming-channel sounds can receive the same environmental EFX send.
- OpenAL EFX environmental reverb is attached to each source when the active audio device supports `ALC_EXT_EFX`. If EFX is unavailable or initialization fails, audio continues without crashing and a warning is logged.
- EFX identifiers are invalidated when Minecraft tears down/reloads its OpenAL device context.
- Mixin configuration and refmap generation are included for Forge's reobfuscated runtime.

## Existing acoustic models

- Multi-ray voxel-world probes estimate obstruction, material transmission, diffraction, scattering, reflections and frequency-dependent low-pass targets.
- Six octave-band material tables classify common stone, wood, glass, foliage, soil, soft and metal blocks.
- Atmospheric loss uses temperature, humidity and pressure; wind is projected along the path.
- Air/water sound-speed and boundary-impedance estimates include same-medium and crossing-medium travel-time paths.
- Room RT60 and rectangular-room modal resonance estimates.
- Two-path interference and knife-edge diffraction utilities.
- Stereo-pan/interaural-delay estimate and aerodynamic-flow estimate for adapters.
- Chunk-safe streaming DSP API with stateful low/high-pass filters, smoothed gain, bounded delay and feedback reverb.
- Bounded motion tracking and same-tick voxel trace cache.

## Current boundaries (important)

The new OpenAL bridge attaches a shared, deliberately mild reverb effect to all Minecraft OpenAL channels. It is a real output-stage effect, not a PCM-only helper. It is **not yet a room-adaptive impulse-response system**: the same EFX reverb preset is currently shared across sources, and the voxel model's per-source `lowPassHz` result is not yet translated into a per-source OpenAL filter. World-positioned sounds do receive calculated obstruction/transmission gain. Relative UI/music sounds are routed through the audio channel hook and receive the shared reverb, but do not receive world occlusion because they have no meaningful world coordinates.

The separate `AcousticApi.processStream(...)` method remains an adapter API for mods that own decoded PCM blocks; the OpenAL EFX path is what currently reaches vanilla static and streaming channels. Minecraft's native decoding, channel allocation, panning and distance rolloff remain in place. This addon does not claim a full physical wave solver or full room impulse response.

## Build and verification

Requirements: JDK 17 and Gradle 8.1.1+.

    gradle --no-daemon clean build

GitHub Actions runs the complete Gradle build (including tests), validates the generated JAR archive, uploads the standalone mod JAR as `BD-Acoustic-Engine-JAR`, and uploads a source/build bundle as `BD-Acoustic-Engine-latest`. It does not publish a GitHub Release.

## Runtime validation still required

The automated build cannot establish whether the effect is audible on a particular OpenAL device or compatible with every other audio mod. In-game validation remains required for indoor steps, door sounds, supermarket ambience, radio behind walls, tunnels, bridges, open air, rivers/shore and moving aircraft. EFX support varies by audio driver; logs will report whether the extension initialized.
