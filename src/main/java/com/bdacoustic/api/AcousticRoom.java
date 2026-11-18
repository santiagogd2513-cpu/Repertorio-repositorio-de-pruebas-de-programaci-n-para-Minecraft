package com.bdacoustic.api;

/** Optional geometry contract for room-aware integrations. Units are metres and square metres. */
public interface AcousticRoom {
    double roomVolumeCubicMetres();
    double roomSurfaceAreaSquareMetres();
    double roomMeanAbsorption();
}