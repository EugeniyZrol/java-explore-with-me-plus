package ru.practicum.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatsController;
import ru.practicum.StatsService;
import ru.practicum.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsControllerTest {

    @Mock
    private StatsService statsService;

    @InjectMocks
    private StatsController statsController;

    @Test
    void saveHit_shouldReturnCreatedStatus() {
        EndpointHitDto inputDto = new EndpointHitDto();
        inputDto.setApp("test-app");
        inputDto.setUri("/test");
        inputDto.setIp("192.168.1.1");
        inputDto.setTimestamp(LocalDateTime.now());

        EndpointHitDto expectedDto = new EndpointHitDto();
        expectedDto.setId(1L);
        expectedDto.setApp("test-app");
        expectedDto.setUri("/test");
        expectedDto.setIp("192.168.1.1");
        expectedDto.setTimestamp(inputDto.getTimestamp());

        when(statsService.saveHit(inputDto)).thenReturn(expectedDto);

        EndpointHitDto result = statsController.saveHit(inputDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(statsService, times(1)).saveHit(inputDto);
    }

    @Test
    void getStats_shouldReturnStats() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = Arrays.asList("/test1", "/test2");
        Boolean unique = false;

        ViewStatsDto stat1 = new ViewStatsDto("app1", "/test1", 10L);
        ViewStatsDto stat2 = new ViewStatsDto("app1", "/test2", 5L);
        List<ViewStatsDto> expectedStats = Arrays.asList(stat1, stat2);

        when(statsService.getStats(start, end, uris, unique)).thenReturn(expectedStats);

        List<ViewStatsDto> result = statsController.getStats(start, end, uris, unique);

        assertEquals(2, result.size());
        verify(statsService, times(1)).getStats(start, end, uris, unique);
    }

    @Test
    void getStats_shouldThrowExceptionWhenStartAfterEnd() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().minusDays(1);
        List<String> uris = Arrays.asList("/test1", "/test2");
        Boolean unique = false;

        assertThrows(IllegalArgumentException.class, () -> {
            statsController.getStats(start, end, uris, unique);
        });

        verify(statsService, never()).getStats(any(), any(), any(), any());
    }
}