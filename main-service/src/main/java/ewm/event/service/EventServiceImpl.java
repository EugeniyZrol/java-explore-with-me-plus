package ewm.event.service;

import ewm.category.mapper.CategoryMapper;
import ewm.category.service.CategoryService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final UserService userService;
    private final CategoryService categoryService;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEvents(Long userId, Pageable pageable) {
        userService.checkUserExists(userId);
        return eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(event -> {
                    EventShortDto eventDto = eventMapper.toShortDto(event);
                    eventDto.setCategory(categoryService.getCategory(event.getCategory().getId()));
                    //TODO: confirmedRequests
                    eventDto.setInitiator(userService.getUserById(event.getInitiator().getId()));
                    //TODO: views
                    return eventDto;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long userId, Long eventId) {
        userService.checkUserExists(userId);
        checkEventExists(eventId);
        Event eventDB = eventRepository.findById(eventId).get();
        EventFullDto eventDto = eventMapper.toFullDto(eventDB);
        eventDto.setCategory(categoryService.getCategory(eventDB.getCategory().getId()));
        //TODO: confirmedRequests
        eventDto.setInitiator(userService.getUserById(eventDB.getInitiator().getId()));
        //TODO: views
        return eventDto;
    }

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User userDB = userMapper.toEntity(userService.getUserById(userId));
        Event eventToSave = eventMapper.toEvent(newEventDto);
        eventToSave.setInitiator(userDB);
        eventToSave.setCreatedAt(LocalDateTime.now());
        eventToSave =  eventRepository.save(eventToSave);
        //TODO: confirmedRequests - or not? we create already
        //TODO: views
        return eventMapper.toFullDto(eventToSave);
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
            categoryService.checkCategoryExists(categoryId);
            eventDB.setCategory(categoryMapper.toCategory(categoryService.getCategory(categoryId)));
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
        //TODO: views
        return eventDto;
    }

    public void checkEventExists(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event not found with id: " + eventId);
        }
    }
}
