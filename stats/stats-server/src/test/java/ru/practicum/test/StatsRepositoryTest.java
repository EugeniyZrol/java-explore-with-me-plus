package ru.practicum.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitEntity;
import ru.practicum.StatsRepository;
import ru.practicum.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
@Transactional
public class StatsRepositoryTest {

    @Autowired
    private StatsRepository statsRepository;

    @BeforeEach
    void setUp() {

        statsRepository.deleteAll();

        LocalDateTime now = LocalDateTime.now();

        EndpointHitEntity hit1 = new EndpointHitEntity();
        hit1.setApp("ewm-main-service");
        hit1.setUri("/events/1");
        hit1.setIp("192.168.1.1");
        hit1.setTimestamp(now.minusHours(2));

        EndpointHitEntity hit2 = new EndpointHitEntity();
        hit2.setApp("ewm-main-service");
        hit2.setUri("/events/1");
        hit2.setIp("192.168.1.2");
        hit2.setTimestamp(now.minusHours(1));

        statsRepository.save(hit1);
        statsRepository.save(hit2);
    }

    @Test
    void findStats_shouldReturnCorrectStats() {
        LocalDateTime start = LocalDateTime.now().minusHours(3);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = List.of("/events/1");

        List<ViewStatsDto> result = statsRepository.findStats(start, end, uris);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUri()).isEqualTo("/events/1");
        assertThat(result.getFirst().getHits()).isEqualTo(2L);
    }

    @Test
    void findUniqueStats_shouldReturnUniqueIPs() {
        LocalDateTime start = LocalDateTime.now().minusHours(3);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = List.of("/events/1");

        List<ViewStatsDto> result = statsRepository.findUniqueStats(start, end, uris);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUri()).isEqualTo("/events/1");
        assertThat(result.getFirst().getHits()).isEqualTo(2L);
    }

    @Test
    void findStats_withEmptyUris_shouldReturnAll() {
        LocalDateTime start = LocalDateTime.now().minusHours(3);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = null;

        List<ViewStatsDto> result = statsRepository.findStats(start, end, uris);

        assertThat(result).hasSize(1);
    }

    @Test
    void findStats_withMultipleUris_shouldReturnCorrect() {
        EndpointHitEntity hit3 = new EndpointHitEntity();
        hit3.setApp("ewm-main-service");
        hit3.setUri("/events/2");
        hit3.setIp("192.168.1.3");
        hit3.setTimestamp(LocalDateTime.now().minusHours(2));
        statsRepository.save(hit3);

        LocalDateTime start = LocalDateTime.now().minusHours(3);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = List.of("/events/1", "/events/2");

        List<ViewStatsDto> result = statsRepository.findStats(start, end, uris);

        assertThat(result).hasSize(2);
    }

    @Test
    void findStats_withDifferentApps_shouldReturnSeparate() {
        EndpointHitEntity hit3 = new EndpointHitEntity();
        hit3.setApp("ewm-other-service");
        hit3.setUri("/events/1");
        hit3.setIp("192.168.1.3");
        hit3.setTimestamp(LocalDateTime.now().minusHours(2));
        statsRepository.save(hit3);

        LocalDateTime start = LocalDateTime.now().minusHours(3);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = List.of("/events/1");

        List<ViewStatsDto> result = statsRepository.findStats(start, end, uris);

        assertThat(result).hasSize(2);
    }

    @Test
    void findStats_withNoHits_shouldReturnEmpty() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        List<String> uris = List.of("/events/1");

        List<ViewStatsDto> result = statsRepository.findStats(start, end, uris);

        assertThat(result).isEmpty();
    }

    @Test
    void findUniqueStats_withSameIP_shouldCountOnce() {
        EndpointHitEntity hit3 = new EndpointHitEntity();
        hit3.setApp("ewm-main-service");
        hit3.setUri("/events/1");
        hit3.setIp("192.168.1.1");
        hit3.setTimestamp(LocalDateTime.now().minusHours(3));
        statsRepository.save(hit3);

        LocalDateTime start = LocalDateTime.now().minusHours(4);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = List.of("/events/1");

        List<ViewStatsDto> result = statsRepository.findUniqueStats(start, end, uris);

        assertThat(result.getFirst().getHits()).isEqualTo(2L); // 2 уникальных IP
    }

}