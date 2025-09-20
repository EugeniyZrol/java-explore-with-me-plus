package ewm.participationRequest.service;

import ewm.event.model.Event;
import ewm.event.repository.EventRepository;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.participationRequest.dto.ParticipationRequestDto;
import ewm.participationRequest.mapper.ParticipationRequestMapper;
import ewm.participationRequest.model.ParticipationRequest;
import ewm.participationRequest.model.RequestStatus;
import ewm.participationRequest.repository.ParticipationRequestRepository;
import ewm.participationRequest.repository.RequestStatusRepository;
import ewm.user.model.User;
import ewm.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestStatusRepository statusRepository;
    private final ParticipationRequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Получение запросов для пользователя с id: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        return requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        log.info("Создание запроса для пользователя с id: {} на событие с id: {}", userId, eventId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        // Проверка: нельзя участвовать в своем событии
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Нельзя участвовать в собственном событии");
        }

        // Проверка: событие должно быть опубликовано
        if (!"PUBLISHED".equals(event.getState())) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }

        // Проверка: нельзя добавить повторный запрос
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Запрос на участие в этом событии уже существует");
        }

        // Проверка: лимит участников
        Long confirmedRequests = requestRepository.countConfirmedRequests(eventId);
        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников для этого события");
        }

        RequestStatus status;
        // Если пре-модерация отключена или лимит 0, автоматически подтверждаем
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            status = statusRepository.findByName("CONFIRMED")
                    .orElseThrow(() -> new IllegalStateException("Статус CONFIRMED не найден"));
        } else {
            status = statusRepository.findByName("PENDING")
                    .orElseThrow(() -> new IllegalStateException("Статус PENDING не найден"));
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .requester(user)
                .event(event)
                .created(LocalDateTime.now())
                .status(status)
                .build();

        ParticipationRequest savedRequest = requestRepository.save(request);
        log.info("Запрос создан с id: {}", savedRequest.getId());

        return requestMapper.toDto(savedRequest);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отмена запроса с id: {} для пользователя с id: {}", requestId, userId);

        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден"));

        RequestStatus canceledStatus = statusRepository.findByName("CANCELED")
                .orElseThrow(() -> new IllegalStateException("Статус CANCELED не найден"));

        request.setStatus(canceledStatus);
        ParticipationRequest updatedRequest = requestRepository.save(request);

        log.info("Запрос с id: {} успешно отменен", requestId);
        return requestMapper.toDto(updatedRequest);
    }
}