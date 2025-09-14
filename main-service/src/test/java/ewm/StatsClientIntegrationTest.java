package ewm;

import client.StatsClient;
import model.EndpointHitDto;
import model.ViewStatsDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@Import(StatsClient.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class StatsClientIntegrationTest {

    @Autowired
    private StatsClient statsClient;

    @Test
    public void testHitAndGetStats() throws InterruptedException {
        try {
            statsClient.hit(new EndpointHitDto(
                    "ewm-main-service",
                    "/events/1",
                    "127.0.0.1",
                    LocalDateTime.now()));

            System.out.println("Hit запрос отправлен");

            Thread.sleep(1000);

            List<ViewStatsDto> result = statsClient.getStats(
                    LocalDateTime.now().minusHours(1),
                    LocalDateTime.now().plusHours(1),
                    java.util.Optional.empty(),
                    true);

            System.out.println("Результат статистики: " + result);

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}