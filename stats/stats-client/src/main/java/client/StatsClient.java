package client;

import model.EndpointHitDto;
import model.ViewStatsDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
public class StatsClient {
    final RestClient restClient;

    public StatsClient(RestClient.Builder restClientBuilder,
                       @Value("${stats.server.url:http://localhost:9090}") String serverUrl) {
        this.restClient = restClientBuilder
                .baseUrl(serverUrl)
                .build();
    }

    public void hit(EndpointHitDto endpointHitDto) {
        restClient.post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(endpointHitDto)
                .retrieve();
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       Optional<List<String>> uris, Boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return restClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/stats")
                            .queryParam("start", start.format(formatter))
                            .queryParam("end", end.format(formatter))
                            .queryParam("unique", unique);

                    uris.ifPresent(uriList -> {
                        if (uriList != null && !uriList.isEmpty()) {
                            uriList.forEach(uri -> builder.queryParam("uris", uri));
                        }
                    });

                    return builder.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}