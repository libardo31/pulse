package com.pulsepass.service;

import com.pulsepass.dto.response.ArtistResponse;

public interface ArtistService {
    ArtistResponse findByStageName(String stageName);
}
