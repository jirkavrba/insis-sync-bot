package cz.vse.fis.discord.insis;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.reactor.http.client.ReactorHttpClient;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Singleton
@RequiredArgsConstructor
public class InsisScrapingService {

    private final ReactorHttpClient client;

    private final Logger logger = LoggerFactory.getLogger(InsisScrapingService.class);

    private static final String INSIS_BASE_URL = "https://insis.vse.cz";

    private static final String INSIS_ENTRY_URL = INSIS_BASE_URL + "/katalog/plany.pl?fakulta=40;lang=cz";

    @NonNull
    public Mono<List<InsisSubject>> scrapeAvailableSubjects() {
        return fetchCatalogueUrl().map(todo -> List.of());
    }

    @NonNull
    private Mono<String> fetchCatalogueUrl() {
        return client.exchange(INSIS_ENTRY_URL, String.class).map(response -> {
            final Document document = Jsoup.parse(response.body());
            final Elements rows = document.select("tr.uis-hl-table.lbn");
            final String url = rows.stream()
                .filter(row -> row.text().startsWith("E"))
                .findFirst()
                .map(row -> Objects.requireNonNull(row.selectFirst("a")).attr("href"))
                .orElseThrow(() -> new IllegalArgumentException("Cannot find the catalogue URL!"));

            logger.info("Found the catalogue URL: {}", url);

            return url;
        });
    }

}
