package ewm;

import client.StatsClient;
import model.EndpointHitDto;
import model.ViewStatsDto;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
@ComponentScan(value = "client")
public class MainApplication {
    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(MainApplication.class, args);
        StatsClient statsClient = context.getBean(StatsClient.class);

        statsClient.hit(new EndpointHitDto(
                "ewm-main-service",
                "/events/1",
                "127.0.0.1",
                LocalDateTime.now()));

        Thread.sleep(500);

        List<ViewStatsDto> result = statsClient.getStats(
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1),
                java.util.Optional.empty(),
                true);

        System.out.println(result);
    }
}
