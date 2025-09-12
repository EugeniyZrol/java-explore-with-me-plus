package client;

import model.EndpointHitDto;
import model.ViewStatsDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class StatsClient {
    final RestClient restClient;

    public StatsClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("http://localhost:9090")
                .build();
    }

    public void hit(EndpointHitDto endpointHitDto) {
        restClient.post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(endpointHitDto)
                .retrieve();
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, Optional<List<String>> uris, Boolean unique) {
        return restClient.get()
                .uri(uriBuilder -> {
                    UriBuilder builder = uriBuilder.path("/stats");

                    builder
                            .queryParam("start", start.toString())
                            .queryParam("end", end.toString())
                            .queryParam("unique", unique);

                    uris.ifPresent(list ->
                            list.forEach(uri -> builder.queryParam("uris", uri)));

                    return builder.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<List<ViewStatsDto>>() {});
    }
}
