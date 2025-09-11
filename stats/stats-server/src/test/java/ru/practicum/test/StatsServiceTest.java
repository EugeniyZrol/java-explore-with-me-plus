package ru.practicum.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private StatsRepository statsRepository;

    @Mock
    private EndpointHitMapper endpointHitMapper;

    @InjectMocks
    private StatsService statsService;

    @Test
    void saveHit_shouldSaveAndReturnDto() {
        EndpointHitDto inputDto = new EndpointHitDto();
        inputDto.setApp("test-app");
        inputDto.setUri("/test");
        inputDto.setIp("192.168.1.1");
        inputDto.setTimestamp(LocalDateTime.now());

        EndpointHitEntity entity = new EndpointHitEntity();
        entity.setId(1L);
        entity.setApp("test-app");
        entity.setUri("/test");
        entity.setIp("192.168.1.1");
        entity.setTimestamp(inputDto.getTimestamp());

        EndpointHitDto expectedDto = new EndpointHitDto();
        expectedDto.setId(1L);
        expectedDto.setApp("test-app");
        expectedDto.setUri("/test");
        expectedDto.setIp("192.168.1.1");
        expectedDto.setTimestamp(inputDto.getTimestamp());

        when(endpointHitMapper.toEntity(inputDto)).thenReturn(entity);
        when(statsRepository.save(entity)).thenReturn(entity);
        when(endpointHitMapper.toDto(entity)).thenReturn(expectedDto);

        EndpointHitDto result = statsService.saveHit(inputDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test-app", result.getApp());
        verify(statsRepository, times(1)).save(entity);
    }

    @Test
    void getStats_shouldCallFindStatsWhenNotUnique() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = Arrays.asList("/test1", "/test2");
        Boolean unique = false;

        ViewStatsDto stat1 = new ViewStatsDto("app1", "/test1", 10L);
        ViewStatsDto stat2 = new ViewStatsDto("app1", "/test2", 5L);
        List<ViewStatsDto> expectedStats = Arrays.asList(stat1, stat2);

        when(statsRepository.findStats(start, end, uris)).thenReturn(expectedStats);

        List<ViewStatsDto> result = statsService.getStats(start, end, uris, unique);

        assertEquals(2, result.size());
        verify(statsRepository, times(1)).findStats(start, end, uris);
        verify(statsRepository, never()).findUniqueStats(any(), any(), any());
    }

    @Test
    void getStats_shouldCallFindUniqueStatsWhenUnique() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = Arrays.asList("/test1", "/test2");
        Boolean unique = true;

        ViewStatsDto stat1 = new ViewStatsDto("app1", "/test1", 8L);
        ViewStatsDto stat2 = new ViewStatsDto("app1", "/test2", 3L);
        List<ViewStatsDto> expectedStats = Arrays.asList(stat1, stat2);

        when(statsRepository.findUniqueStats(start, end, uris)).thenReturn(expectedStats);

        List<ViewStatsDto> result = statsService.getStats(start, end, uris, unique);

        assertEquals(2, result.size());
        verify(statsRepository, times(1)).findUniqueStats(start, end, uris);
        verify(statsRepository, never()).findStats(any(), any(), any());
    }
}