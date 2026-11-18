# BD Acoustic Engine

Independent acoustic-engine addon for Minecraft Java 1.20.1 / Forge 47.4.5 / Java 17. It is designed to be integrated with Beautiful Decor later; this repository does not modify or bundle Beautiful Decor.

## Included in this Phase 1 foundation
- Forge mod bootstrap and client configuration.
- A reusable acoustic API for other mods to submit positional sources and velocity.
- Distance attenuation estimate, temperature/humidity speed-of-sound estimate, travel-time estimate and Doppler ratio.
- Multi-ray block-path probing with approximate material absorption, obstruction, reflection count and frequency-dependent low-pass estimate.
- Air/water medium selection with a separate underwater sound-speed approximation.
- Stateful chunk-safe low-pass DSP utility with bounded per-stream state.
- Client sound-event observation hook. It evaluates positional sounds while leaving Minecraft's vanilla mixer in control to avoid duplicate playback or unsafe replacement of third-party streams.

## Important implementation boundary
The first-phase hook currently **observes and evaluates** sound events; it does not yet inject delayed playback, apply the computed gain/filter to Minecraft's final mixer, or synthesize reflection taps. The DSP class is an integration-ready processing primitive, not yet wired into the game's internal streaming buffers. True wave diffraction/interference, room impulse response convolution, moving-source history, wind-field simulation, and physically complete underwater boundary refraction remain future implementation work.

## Build
Run with JDK 17 and Gradle 8.1+:
`gradle --no-daemon clean build`

GitHub Actions builds on pushes and uploads a source/build artifact. The workflow does not publish Releases.
