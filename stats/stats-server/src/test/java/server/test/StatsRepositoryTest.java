package server.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import server.entity.EndpointHitEntity;
import server.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=password",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class StatsRepositoryTest {

    @Autowired
    private StatsRepository statsRepository;

    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime start = now.minusHours(1);
    private final LocalDateTime end = now.plusHours(1);

    @BeforeEach
    void setUp() {
        statsRepository.deleteAll();
    }

    @Test
    void testSaveEndpointHit() {
        EndpointHitEntity entity = createEntity("test-app", "/test", "192.168.1.1", now);

        EndpointHitEntity saved = statsRepository.save(entity);

        assertNotNull(saved.getId());
        assertEquals("test-app", saved.getApp());
        assertEquals("/test", saved.getUri());
        assertEquals("192.168.1.1", saved.getIp());
        assertEquals(now, saved.getTimestamp());
    }

    @Test
    void testFindStats_WithUris() {
        createAndSaveEntities();

        var result = statsRepository.findStats(start, end, List.of("/api/events/1", "/api/events/2"));

        assertEquals(2, result.size());
        assertEquals("/api/events/1", result.get(0).getUri());
        assertEquals(3L, result.get(0).getHits()); // 3 hits for /api/events/1
        assertEquals("/api/events/2", result.get(1).getUri());
        assertEquals(2L, result.get(1).getHits()); // 2 hits for /api/events/2
    }

    @Test
    void testFindStats_WithoutUris() {
        createAndSaveEntities();

        var result = statsRepository.findStats(start, end, null);

        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getUri().equals("/api/events/1") && dto.getHits() == 3));
        assertTrue(result.stream().anyMatch(dto -> dto.getUri().equals("/api/events/2") && dto.getHits() == 2));
        assertTrue(result.stream().anyMatch(dto -> dto.getUri().equals("/api/events/3") && dto.getHits() == 1));
    }

    @Test
    void testFindStats_TimeRangeFilter() {
        createAndSaveEntities();

        var result = statsRepository.findStats(
                now.plusHours(2),
                now.plusHours(3),
                List.of("/api/events/1")
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void testFindUniqueStats_WithUris() {
        createAndSaveEntities();

        var result = statsRepository.findUniqueStats(
                start,
                end,
                List.of("/api/events/1", "/api/events/2")
        );

        assertEquals(2, result.size());
        assertEquals("/api/events/1", result.get(0).getUri());
        assertEquals(2L, result.get(0).getHits()); // 2 unique IPs for /api/events/1
        assertEquals("/api/events/2", result.get(1).getUri());
        assertEquals(2L, result.get(1).getHits()); // 2 unique IPs for /api/events/2
    }

    @Test
    void testFindUniqueStats_WithoutUris() {
        createAndSaveEntities();

        var result = statsRepository.findUniqueStats(start, end, null);

        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getUri().equals("/api/events/1") && dto.getHits() == 2));
        assertTrue(result.stream().anyMatch(dto -> dto.getUri().equals("/api/events/2") && dto.getHits() == 2));
        assertTrue(result.stream().anyMatch(dto -> dto.getUri().equals("/api/events/3") && dto.getHits() == 1));
    }

    @Test
    void testFindUniqueStats_DuplicateIPs() {
        statsRepository.save(createEntity("app1", "/test", "192.168.1.1", now));
        statsRepository.save(createEntity("app1", "/test", "192.168.1.1", now.plusMinutes(5)));
        statsRepository.save(createEntity("app1", "/test", "192.168.1.2", now.plusMinutes(10)));

        var result = statsRepository.findUniqueStats(start, end, List.of("/test"));

        assertEquals(1, result.size());
        assertEquals("/test", result.get(0).getUri());
        assertEquals(2L, result.get(0).getHits()); // 2 unique IPs
    }

    private void createAndSaveEntities() {
        statsRepository.save(createEntity("app1", "/api/events/1", "192.168.1.1", now));
        statsRepository.save(createEntity("app1", "/api/events/1", "192.168.1.2", now.plusMinutes(5)));
        statsRepository.save(createEntity("app1", "/api/events/1", "192.168.1.1", now.plusMinutes(10)));

        statsRepository.save(createEntity("app1", "/api/events/2", "192.168.1.3", now.plusMinutes(15)));
        statsRepository.save(createEntity("app1", "/api/events/2", "192.168.1.4", now.plusMinutes(20)));

        statsRepository.save(createEntity("app2", "/api/events/3", "192.168.1.5", now.plusMinutes(25)));
    }

    private EndpointHitEntity createEntity(String app, String uri, String ip, LocalDateTime timestamp) {
        EndpointHitEntity entity = new EndpointHitEntity();
        entity.setApp(app);
        entity.setUri(uri);
        entity.setIp(ip);
        entity.setTimestamp(timestamp);
        return entity;
    }
}