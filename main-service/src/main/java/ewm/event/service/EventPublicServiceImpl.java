package ewm.event.service;

import ewm.event.dto.EventFullDto;
import ewm.event.dto.EventShortDto;
import ewm.event.mapper.EventMapper;
import ewm.event.model.Event;
import ewm.event.model.EventState;
import ewm.event.repository.EventRepository;
import ewm.participationRequest.service.ConfirmedRequestsService;
import ewm.util.PageUtils;
import ewm.exception.NotFoundException;
import client.StatsClient;
import model.EndpointHitDto;
import model.ViewStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ewm.event.service.PublicEventSpecs.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventPublicServiceImpl implements EventPublicService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final ConfirmedRequestsService confirmedRequestsService;
    private final StatsClient statsClient;

    private static final String DATE_TIME_FMT = "yyyy-MM-dd HH:mm:ss";

    @Override
    public List<EventShortDto> getPublicEvents(String text,
                                               List<Long> categories,
                                               Boolean paid,
                                               LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd,
                                               boolean onlyAvailable,
                                               String sort,
                                               int from,
                                               int size,
                                               String ip,
                                               String uri) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = (rangeStart != null) ? rangeStart : now;
        LocalDateTime end = rangeEnd;
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("rangeEnd must not be before rangeStart");
        }

        Pageable page = PageUtils.offsetBased(from, size);
        List<Event> events = eventRepository.findAll(
                published()
                        .and(text(text))
                        .and(categories(categories))
                        .and(paid(paid))
                        .and(dateBetween(start, end))
                        .and(onlyAvailable ? hasSlots() : null)
                , page).getContent();

        if (events.isEmpty()) {
            saveHit(ip, uri);
            return List.of();
        }

        Map<Long, Long> confirmed = confirmedRequestsService.getConfirmedCount(events.stream()
                .map(Event::getId).collect(Collectors.toSet()));

        Map<Long, Long> views = getViews(events);

        if ("VIEWS".equalsIgnoreCase(sort)) {
            events.sort(Comparator.comparingLong((Event e) -> views.getOrDefault(e.getId(), 0L)).reversed()
                    .thenComparing(Event::getEventDate));
        } else {
            events.sort(Comparator.comparing(Event::getEventDate));
        }

        List<EventShortDto> result = events.stream()
                .map(e -> {
                    EventShortDto dto = eventMapper.toShortDto(e);
                    dto.setViews(views.getOrDefault(e.getId(), 0L));
                    dto.setConfirmedRequests(confirmed.getOrDefault(e.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());

        saveHit(ip, uri);
        return result;
    }

    @Override
    public EventFullDto getPublicEvent(Long eventId, String ip, String uri) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        Map<Long, Long> confirmed = confirmedRequestsService.getConfirmedCount(Set.of(event.getId()));
        Map<Long, Long> views = getViews(List.of(event));

        EventFullDto dto = eventMapper.toFullDto(event);
        dto.setViews(views.getOrDefault(event.getId(), 0L));
        dto.setConfirmedRequests(confirmed.getOrDefault(event.getId(), 0L));

        saveHit(ip, uri);
        return dto;
    }

    private void saveHit(String ip, String uri) {
        EndpointHitDto hit = new EndpointHitDto();
        hit.setApp("ewm-main-service");
        hit.setIp(ip);
        hit.setUri(uri);
        hit.setTimestamp(LocalDateTime.now());
        statsClient.hit(hit);
    }

    private Map<Long, Long> getViews(List<Event> events) {
        if (events.isEmpty()) return Map.of();

        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .collect(Collectors.toList());

        LocalDateTime start = LocalDateTime.of(2000, 1, 1, 0, 0); // достаточно ранняя дата
        LocalDateTime end = LocalDateTime.now();

        List<ViewStatsDto> stats = statsClient.getStats(start, end, Optional.of(uris), true);
        Map<String, Long> byUri = stats.stream()
                .collect(Collectors.toMap(ViewStatsDto::getUri, s -> s.getHits().longValue(), Long::sum));

        Map<Long, Long> result = new HashMap<>();
        for (Event e : events) {
            result.put(e.getId(), byUri.getOrDefault("/events/" + e.getId(), 0L));
        }
        return result;
    }
}
