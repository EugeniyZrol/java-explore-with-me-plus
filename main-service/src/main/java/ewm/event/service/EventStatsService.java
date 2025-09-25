package ewm.event.service;

import client.StatsClient;
import ewm.event.dto.EventFullDto;
import ewm.event.dto.EventShortDto;
import ewm.event.mapper.EventMapper;
import ewm.event.model.Event;
import ewm.participationRequest.service.ConfirmedRequestsService;
import lombok.RequiredArgsConstructor;
import model.EndpointHitDto;
import model.ViewStatsDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EventStatsService {
    private static final String ENDPOINT = "/events";
    private static final String APP_NAME = "ewm-main-service";

    private final StatsClient statsClient;
    private final ConfirmedRequestsService confirmedRequestsService;

    public long getViewsForEvent(Long eventId) {
        String uri = ENDPOINT + "/" + eventId;
        LocalDateTime start = LocalDateTime.now().minusYears(10);
        LocalDateTime end = LocalDateTime.now().plusYears(10);

        List<ViewStatsDto> stats = statsClient.getStats(start, end, Optional.of(List.of(uri)), true);

        return (stats != null && !stats.isEmpty()) ? stats.getFirst().getHits() : 0L;
    }

    public void recordHit(String uri, String ip) {
        statsClient.hit(new EndpointHitDto(APP_NAME, uri, ip, LocalDateTime.now()));
    }

    public Long getConfirmedRequests(Long eventId) {
        return confirmedRequestsService.getConfirmedRequestsCount(eventId);
    }

    public EventFullDto enrichEventFullDto(Event event, EventMapper eventMapper) {
        EventFullDto dto = eventMapper.toFullDto(event);
        dto.setConfirmedRequests(getConfirmedRequests(event.getId()));
        dto.setViews(getViewsForEvent(event.getId()));
        return dto;
    }

    public EventShortDto enrichEventShortDto(Event event, EventMapper eventMapper) {
        EventShortDto dto = eventMapper.toShortDto(event);
        dto.setConfirmedRequests(getConfirmedRequests(event.getId()));
        dto.setViews(getViewsForEvent(event.getId()));
        return dto;
    }
}