import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final StatsRepository statsRepository;
    private final EndpointHitMapper endpointHitMapper;

    public EndpointHit saveHit(EndpointHit endpointHit) {
        EndpointHitEntity entity = endpointHitMapper.toEntity(endpointHit);
        EndpointHitEntity savedEntity = statsRepository.save(entity);
        return endpointHitMapper.toDto(savedEntity);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (Boolean.TRUE.equals(unique)) {
            return statsRepository.findUniqueStats(start, end, uris);
        } else {
            return statsRepository.findStats(start, end, uris);
        }
    }
}