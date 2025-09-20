package ewm.participationRequestTest;

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
import ewm.participationRequest.service.ParticipationRequestServiceImpl;
import ewm.user.model.User;
import ewm.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipationRequestServiceImplTest {

    @Mock
    private ParticipationRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RequestStatusRepository statusRepository;

    @Mock
    private ParticipationRequestMapper requestMapper;

    @InjectMocks
    private ParticipationRequestServiceImpl requestService;

    private User user;
    private Event event;
    private RequestStatus canceledStatus;
    private ParticipationRequest request;
    private ParticipationRequestDto requestDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");

        event = new Event();
        event.setId(1L);
        event.setTitle("Test Event");
        event.setInitiator(user);
        event.setState("PUBLISHED");
        event.setRequestModeration(true);
        event.setParticipantLimit(10);

        RequestStatus pendingStatus = new RequestStatus();
        pendingStatus.setId((byte) 1);
        pendingStatus.setName("PENDING");

        RequestStatus confirmedStatus = new RequestStatus();
        confirmedStatus.setId((byte) 2);
        confirmedStatus.setName("CONFIRMED");

        canceledStatus = new RequestStatus();
        canceledStatus.setId((byte) 3);
        canceledStatus.setName("CANCELED");

        request = ParticipationRequest.builder()
                .id(1L)
                .requester(user)
                .event(event)
                .created(LocalDateTime.now())
                .status(pendingStatus)
                .build();

        requestDto = ParticipationRequestDto.builder()
                .id(1L)
                .requester(1L)
                .event(1L)
                .status("PENDING")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetUserRequests_Success() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findAllByRequesterId(1L)).thenReturn(List.of(request));
        when(requestMapper.toDto(request)).thenReturn(requestDto);

        // When
        List<ParticipationRequestDto> result = requestService.getUserRequests(1L);

        // Then
        assertEquals(1, result.size());
        assertEquals(requestDto, result.getFirst());
        verify(userRepository).existsById(1L);
        verify(requestRepository).findAllByRequesterId(1L);
    }

    @Test
    void testGetUserRequests_UserNotFound() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(NotFoundException.class, () -> requestService.getUserRequests(1L));
        verify(userRepository).existsById(1L);
        verifyNoInteractions(requestRepository);
    }

    @Test
    void testCreateRequest_OwnEvent() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        // When & Then
        assertThrows(ConflictException.class, () -> requestService.createRequest(1L, 1L));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void testCreateRequest_UnpublishedEvent() {
        // Given
        event.setState("PENDING");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        // When & Then
        assertThrows(ConflictException.class, () -> requestService.createRequest(1L, 1L));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void testCancelRequest_Success() {
        // Given
        when(requestRepository.findByIdAndRequesterId(1L, 1L)).thenReturn(Optional.of(request));
        when(statusRepository.findByName("CANCELED")).thenReturn(Optional.of(canceledStatus));
        when(requestRepository.save(request)).thenReturn(request);
        when(requestMapper.toDto(request)).thenReturn(requestDto);

        // When
        ParticipationRequestDto result = requestService.cancelRequest(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(requestDto, result);
        verify(requestRepository).save(request);
    }

    @Test
    void testCancelRequest_RequestNotFound() {
        // Given
        when(requestRepository.findByIdAndRequesterId(1L, 1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> requestService.cancelRequest(1L, 1L));
        verify(requestRepository, never()).save(any());
    }
}