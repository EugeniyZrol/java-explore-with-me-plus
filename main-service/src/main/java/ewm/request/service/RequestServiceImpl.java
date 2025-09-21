package ewm.request.service;

import ewm.request.dto.EventRequestStatusUpdateRequest;
import ewm.request.dto.EventRequestStatusUpdateResult;
import ewm.request.dto.ParticipationRequestDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequestServiceImpl implements RequestService {
    @Override
    public List<ParticipationRequestDto> getRequestsByEvent(Long userId, Long eventId) {
        return List.of();
    }

    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        return null;
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByUser(Long userId) {
        return List.of();
    }

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        return null;
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        return null;
    }
}
