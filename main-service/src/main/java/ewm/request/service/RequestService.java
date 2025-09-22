package ewm.request.service;

import ewm.request.dto.EventRequestStatusUpdateRequest;
import ewm.request.dto.EventRequestStatusUpdateResult;
import ewm.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    List<ParticipationRequestDto> getRequestsByEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest request);

    List<ParticipationRequestDto> getRequestsByUser(Long userId);

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);
}
