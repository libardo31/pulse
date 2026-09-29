package com.pulsepass.service;

import com.pulsepass.domain.Artist;
import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.ArtistMapper;
import com.pulsepass.repository.ArtistRepository;
import com.pulsepass.service.impl.ArtistServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistServiceImplTest {

    @Mock ArtistRepository repository;
    @Mock ArtistMapper mapper;
    @InjectMocks ArtistServiceImpl service;

    @Test
    void artistaExistenteRetornaDto() {
        Artist artist = new Artist("Solar Beat", "Colombia", "Electronic", true);
        ArtistResponse response = new ArtistResponse(1L, "Solar Beat", "Colombia", "Electronic", true);
        when(repository.findByStageNameIgnoreCase("Solar Beat")).thenReturn(Optional.of(artist));
        when(mapper.toResponse(artist)).thenReturn(response);

        assertThat(service.findByStageName("Solar Beat")).isEqualTo(response);
        verify(mapper).toResponse(artist);
    }

    @Test
    void artistaInexistenteLanzaResourceNotFoundException() {
        when(repository.findByStageNameIgnoreCase("Unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByStageName("Unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Artist not found: Unknown");
    }
}
