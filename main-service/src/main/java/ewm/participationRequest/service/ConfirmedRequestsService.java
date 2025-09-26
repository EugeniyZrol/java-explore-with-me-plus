package ewm.participationRequest.service;

import ewm.participationRequest.repository.ParticipationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ConfirmedRequestsService {
    private final ParticipationRequestRepository requestRepository;

    public Long getConfirmedRequestsCount(Long eventId) {
        Long count = requestRepository.countConfirmedRequests(eventId);
        return count != null ? count : 0L;
    }
}