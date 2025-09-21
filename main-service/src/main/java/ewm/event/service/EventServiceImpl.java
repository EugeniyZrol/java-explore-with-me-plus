package ewm.event.service;

import client.StatsClient;
import ewm.categories.mapper.CategoryMapper;
import ewm.categories.service.CategoryService;
import ewm.event.dto.EventFullDto;
import ewm.event.dto.EventShortDto;
import ewm.event.dto.NewEventDto;
import ewm.event.dto.UpdateEventUserRequest;
import ewm.event.mapper.EventMapper;
import ewm.event.model.Event;
import ewm.event.model.EventState;
import ewm.event.model.StateAction;
import ewm.event.repository.EventRepository;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.user.mapper.UserMapper;
import ewm.user.model.User;
import ewm.user.service.UserService;
import lombok.RequiredArgsConstructor;
import model.EndpointHitDto;
import model.ViewStatsDto;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final UserMapper userMapper;
    private final UserService userService;
    private final CategoryService categoryService;
    private final StatsClient statsClient;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEvents(Long userId, Pageable pageable) {
        userService.checkUserExists(userId);

        List<String> uris = new ArrayList<>();

        // getting EventShortDto's without confirmedRequests and views
        List<EventShortDto> eventDtos = eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(event -> {
                    EventShortDto eventDto = eventMapper.toShortDto(event);
                    eventDto.setCategory(categoryService.getCategoryById(event.getCategory().getId()));
                    //TODO: confirmedRequests
                    eventDto.setInitiator(userService.getUserById(event.getInitiator().getId()));
                    uris.add(new StringBuilder(ENDPOINT).append("/").append(event.getId()).toString());
                    return eventDto;
                })
                .toList();

        Map<String, Long> viewsMap = getViewsMap(uris);

        // initializing views
        eventDtos.forEach(eventShortDto -> {
            String uri = new StringBuilder(ENDPOINT).append("/").append(eventShortDto.getId()).toString();
            eventShortDto.setViews(viewsMap.getOrDefault(uri, 0L));
        });
        return eventDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long userId, Long eventId, String ip) {
        userService.checkUserExists(userId);
        checkEventExists(eventId);
        Event eventDB = eventRepository.findById(eventId).get();
        EventFullDto eventDto = eventMapper.toFullDto(eventDB);
        eventDto.setCategory(categoryService.getCategoryById(eventDB.getCategory().getId()));
        //TODO: confirmedRequests
        eventDto.setInitiator(userService.getUserById(eventDB.getInitiator().getId()));

        String uri = ENDPOINT + "/" + eventId;
        Map<String, Long> viewsMap = getViewsMap(List.of(uri));

        Long views = viewsMap.getOrDefault(uri, 0L);
        eventDto.setViews(views);

        statsClient.hit(new EndpointHitDto(APP_NAME, uri, ip, LocalDateTime.now()));
        return eventDto;
    }

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User userDB = userMapper.toEntity(userService.getUserById(userId));
        Event eventToSave = eventMapper.toEvent(newEventDto);
        eventToSave.setInitiator(userDB);
        eventToSave.setCreatedAt(LocalDateTime.now());
        eventToSave.setState(String.valueOf(EventState.PENDING));
        eventToSave =  eventRepository.save(eventToSave);
        EventFullDto eventDto = eventMapper.toFullDto(eventToSave);
        eventDto.setCategory(categoryService.getCategoryById(eventToSave.getCategory().getId()));
        eventDto.setInitiator(userService.getUserById(eventToSave.getInitiator().getId()));
        eventDto.setViews(0L);
        return eventDto;
    }

    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        userService.checkUserExists(userId);
        checkEventExists(eventId);

        Event eventDB = eventRepository.findById(eventId).get();
        EventState currentEventState = EventState.valueOf(eventDB.getState());

        if (EventState.PUBLISHED.equals(currentEventState)) {
            throw new ConflictException("Изменить можно только отмененные события или события в состоянии ожидания " +
                    "модерации");
        }

        if (!request.isAnnotationEmpty()) {
            eventDB.setAnnotation(request.getAnnotation());
        }

        if (!request.isCategoryEmpty()) {
            Long categoryId = request.getCategory();
            eventDB.setCategory(CategoryMapper.toCategory(categoryService.getCategoryById(categoryId)));
        }

        if (!request.isDescriptionEmpty()) {
            eventDB.setDescription(request.getDescription());
        }

        if (!request.isEventDateEmpty()) {
            eventDB.setEventDate(request.getEventDate());
        }

        if (!request.isLocationEmpty()) {
            eventDB.setLocation(eventMapper.toPGpoint(request.getLocation()));
        }

        if (!request.isPaidEmpty()) {
            eventDB.setIsPaid(request.getPaid());
        }

        if (!request.isParticipantLimitEmpty()) {
            eventDB.setParticipantLimit(request.getParticipantLimit());
        }

        if (!request.isRequestModerationEmpty()) {
            eventDB.setIsRequestModeration(request.getRequestModeration());
        }

        if (!request.isStateActionEmpty()) {
            switch (StateAction.valueOf(request.getStateAction())) {
                case SEND_TO_REVIEW ->  eventDB.setState(String.valueOf(EventState.PENDING));
                case CANCEL_REVIEW ->  eventDB.setState(String.valueOf(EventState.CANCELED));
            }

        }

        if (!request.isTitleEmpty()) {
            eventDB.setTitle(request.getTitle());
        }

        EventFullDto eventDto = eventMapper.toFullDto(eventRepository.save(eventDB));
        //TODO: confirmedRequests

        eventDto.setCategory(categoryService.getCategoryById(eventDB.getCategory().getId()));
        eventDto.setInitiator(userService.getUserById(eventDB.getInitiator().getId()));

        String uri = ENDPOINT + "/" + eventId;
        Map<String, Long> viewsMap = getViewsMap(List.of(uri));

        Long views = viewsMap.getOrDefault(uri, 0L);
        eventDto.setViews(views);
        return eventDto;
    }

    public void checkEventExists(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event not found with id: " + eventId);
        }
    }

    private Map<String, Long> getViewsMap(List<String> uris) {
        // getting statistics
        List<ViewStatsDto> viewStatsDtos = statsClient.getStats(
                LocalDateTime.now().minusYears(10),
                LocalDateTime.now(),
                Optional.of(uris),
                false
        );

        return viewStatsDtos.stream()
                .collect(Collectors.toMap(
                        ViewStatsDto::getUri,
                        ViewStatsDto::getHits
         ));
    }
}
