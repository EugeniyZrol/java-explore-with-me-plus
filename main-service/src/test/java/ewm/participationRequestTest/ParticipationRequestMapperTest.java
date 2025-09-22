package ewm.participationRequestTest;

import ewm.participationRequest.dto.ParticipationRequestDto;
import ewm.participationRequest.mapper.ParticipationRequestMapper;
import ewm.participationRequest.model.ParticipationRequest;
import ewm.participationRequest.model.RequestStatus;
import ewm.event.model.Event;
import ewm.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class ParticipationRequestMapperTest {

    @Autowired
    private ParticipationRequestMapper requestMapper;

    @Test
    void testToDto() {
        // Given
        User user = new User();
        user.setId(1L);

        Event event = new Event();
        event.setId(2L);

        RequestStatus status = new RequestStatus();
        status.setName("PENDING");

        ParticipationRequest request = ParticipationRequest.builder()
                .id(10L)
                .requester(user)
                .event(event)
                .created(LocalDateTime.of(2023, 10, 15, 12, 30))
                .status(status)
                .build();

        // When
        ParticipationRequestDto dto = requestMapper.toDto(request);

        // Then
        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals(1L, dto.getRequester());
        assertEquals(2L, dto.getEvent());
        assertEquals("PENDING", dto.getStatus());
        assertEquals(LocalDateTime.of(2023, 10, 15, 12, 30), dto.getCreated());
    }

    @Test
    void testToEntity() {
        // Given
        ParticipationRequestDto dto = ParticipationRequestDto.builder()
                .id(10L)
                .requester(1L)
                .event(2L)
                .status("PENDING")
                .created(LocalDateTime.now())
                .build();

        // When
        ParticipationRequest entity = requestMapper.toEntity(dto);

        // Then
        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getRequester());
        assertNull(entity.getEvent());
        assertNull(entity.getStatus());
        assertNull(entity.getCreated());
    }

    @Test
    void testToDtoWithNullValues() {
        // Given
        ParticipationRequest request = new ParticipationRequest();

        // When
        ParticipationRequestDto dto = requestMapper.toDto(request);

        // Then
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getRequester());
        assertNull(dto.getEvent());
        assertNull(dto.getStatus());
        assertNull(dto.getCreated());
    }
}