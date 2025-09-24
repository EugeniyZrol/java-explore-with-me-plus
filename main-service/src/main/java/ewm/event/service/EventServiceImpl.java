package ewm.event.service;

import client.StatsClient;
import ewm.participationRequest.service.ConfirmedRequestsService;
import ewm.event.dto.*;
import ewm.event.mapper.EventMapper;
import ewm.event.model.Event;
import ewm.event.model.EventState;
import ewm.event.model.StateAction;
import ewm.event.repository.EventRepository;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.user.model.User;
import ewm.user.service.UserService;
import lombok.RequiredArgsConstructor;
import model.EndpointHitDto;
import model.ViewStatsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {
    private static final String ENDPOINT = "/events";
    private static final String APP_NAME = "ewm-main-service";

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserService userService;
    private final StatsClient statsClient;
    private final ConfirmedRequestsService confirmedRequestsService;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEvents(Long userId, Pageable pageable) {
        userService.getUserById(userId);

        Page<Event> eventsPage = eventRepository.findAllByInitiatorId(userId, pageable);
        List<Event> events = eventsPage.getContent();

        List<String> uris = events.stream()
                .map(event -> ENDPOINT + "/" + event.getId())
                .collect(Collectors.toList());

        Map<String, Long> viewsMap = getViewsMap(uris);

        return events.stream()
                .map(event -> {
                    EventShortDto eventDto = eventMapper.toShortDto(event);

                    Long confirmedRequests = confirmedRequestsService.getConfirmedRequestsCount(event.getId());
                    eventDto.setConfirmedRequests(confirmedRequests);

                    String uri = ENDPOINT + "/" + event.getId();
                    eventDto.setViews(viewsMap.getOrDefault(uri, 0L));

                    return eventDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long userId, Long eventId, String ip) {
        userService.getUserById(userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        EventFullDto eventDto = eventMapper.toFullDto(event);

        Long confirmedRequests = confirmedRequestsService.getConfirmedRequestsCount(eventId);
        eventDto.setConfirmedRequests(confirmedRequests);

        String uri = ENDPOINT + "/" + eventId;
        Map<String, Long> viewsMap = getViewsMap(List.of(uri));
        eventDto.setViews(viewsMap.getOrDefault(uri, 0L));

        statsClient.hit(new EndpointHitDto(APP_NAME, uri, ip, LocalDateTime.now()));

        return eventDto;
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Дата начала события должна быть не ранее чем через 2 часа");
        }

        User user = userService.getUserEntityById(userId);

        Event event = eventMapper.toEvent(newEventDto);
        event.setInitiator(user);
        event.setCreatedAt(LocalDateTime.now());
        event.setState(EventState.PENDING.toString());

        Event savedEvent = eventRepository.save(event);
        EventFullDto eventDto = eventMapper.toFullDto(savedEvent);

        eventDto.setConfirmedRequests(0L);
        eventDto.setViews(0L);

        return eventDto;
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        userService.getUserById(userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        EventState currentState = EventState.valueOf(event.getState());

        if (currentState == EventState.PUBLISHED) {
            throw new ConflictException("Изменить можно только отмененные события или события в состоянии ожидания модерации");
        }

        if (request.getEventDate() != null && request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Дата начала события должна быть не ранее чем через 2 часа");
        }

        eventMapper.updateEventFromUserRequest(request, event);

        if (request.getStateAction() != null) {
            StateAction stateAction = StateAction.valueOf(request.getStateAction());
            if (stateAction == StateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING.toString());
            } else if (stateAction == StateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED.toString());
            }
        }

        Event updatedEvent = eventRepository.save(event);
        EventFullDto eventDto = eventMapper.toFullDto(updatedEvent);

        Long confirmedRequests = confirmedRequestsService.getConfirmedRequestsCount(eventId);
        eventDto.setConfirmedRequests(confirmedRequests);

        String uri = ENDPOINT + "/" + eventId;
        Map<String, Long> viewsMap = getViewsMap(List.of(uri));
        eventDto.setViews(viewsMap.getOrDefault(uri, 0L));

        return eventDto;
    }

    private Map<String, Long> getViewsMap(List<String> uris) {
        List<ViewStatsDto> viewStatsDtos = statsClient.getStats(
                LocalDateTime.now().minusYears(10),
                LocalDateTime.now().plusHours(1),
                uris.isEmpty() ? Optional.empty() : Optional.of(uris),
                false
        );

        return viewStatsDtos.stream()
                .collect(Collectors.toMap(
                        ViewStatsDto::getUri,
                        ViewStatsDto::getHits
                ));
    }
}