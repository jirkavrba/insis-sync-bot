package cz.vse.fis.discord.insis;

import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.reactor.http.client.ReactorHttpClient;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Singleton
@RequiredArgsConstructor
public class InsisScrapingService {

    private final int parallelism;

    private final ReactorHttpClient client;

    private final Logger logger = LoggerFactory.getLogger(InsisScrapingService.class);

    private static final String INSIS_BASE_URL = "https://insis.vse.cz";

    private static final String INSIS_ENTRY_URL = INSIS_BASE_URL + "/katalog/plany.pl?fakulta=40;lang=cz";

    private static final Set<String> SCRAPED_SUBJECT_GROUP_PREFIXES = Set.of(
        "oP ",
        "oSZ",
        "oV ",
        "hV ",
        "hP "
    );

    public InsisScrapingService(
        @NonNull ReactorHttpClient client,
        @NonNull @Value("${insis.scraper.parallelism}") Integer parallelism
    ) {
        this.client = client;
        this.parallelism = parallelism;
    }

    @NonNull
    public Mono<Set<InsisSubject>> scrapeAvailableSubjects() {
        return fetchCatalogueUrl()
            .publishOn(Schedulers.newParallel("subjects-scraping", parallelism))
            .flatMapMany(this::fetchStudyTypeUrls)
            .flatMap(this::fetchStudyProgrammeUrls)
            .flatMap(this::fetchSubjectsListingUrl)
            .flatMap(this::fetchSubjects)
            .collectList()
            .map(HashSet::new);
    }

    @NonNull
    private Mono<String> fetchCatalogueUrl() {
        return client.exchange(INSIS_ENTRY_URL, String.class).map(response -> {
            final Document document = Jsoup.parse(response.body());
            final Elements rows = document.select("tr.uis-hl-table.lbn");

            return rows.stream()
                .filter(row -> row.text().startsWith("E"))
                .findFirst()
                .map(row -> Objects.requireNonNull(row.selectFirst("a")).attr("href"))
                .orElseThrow(() -> new IllegalArgumentException("Cannot find the catalogue URL!"));
        });
    }

    @NonNull
    private Flux<String> fetchStudyTypeUrls(final @NonNull String url) {
        return client.exchange(expandInsisUrl(url), String.class).flatMapMany(response -> {
            final Document document = Jsoup.parse(response.body());
            final List<String> links =
                document.select("tr.uis-hl-table.lbn a")
                    .stream()
                    .map(link -> link.attr("href"))
                    .toList();

            return Flux.fromIterable(links);
        });
    }

    @NonNull
    private Flux<String> fetchStudyProgrammeUrls(final @NonNull String url) {
        return client.exchange(expandInsisUrl(url), String.class).flatMapMany(response -> {
            final Document document = Jsoup.parse(response.body());
            final List<String> links =
                document.select("table")
                    .get(1)
                    .select("a")
                    .stream()
                    .map(link -> link.attr("href"))
                    .filter(link -> !link.contains("info=1"))
                    .toList();

            return Flux.fromIterable(links);
        });
    }

    @NonNull
    private Mono<String> fetchSubjectsListingUrl(final @NonNull String url) {
        return client.exchange(expandInsisUrl(url), String.class).map(response -> {
            final Document document = Jsoup.parse(response.body());
            final Element link = Objects.requireNonNull(document.selectFirst("tr.lbn a"));

            return link.attr("href");
        });
    }

    @NonNull
    private Flux<InsisSubject> fetchSubjects(final @NonNull String url) {
        // Imagine non retarded HTML inside InSIS, life could be dream
        return client.exchange(expandInsisUrl(url), String.class)
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(response ->
                Mono.fromSupplier(() -> Jsoup.parse(response.body()))
                    .subscribeOn(Schedulers.boundedElastic())
            )
            .flatMapMany(document -> {
                final Elements headings = document.select("table tr.zahlavi");
                final List<InsisSubject> subjects = headings.stream()
                    .map(Element::nextElementSibling)
                    .filter(Objects::nonNull)
                    .filter(title -> SCRAPED_SUBJECT_GROUP_PREFIXES.contains(
                        title
                            .text()
                            .trim()
                            .substring(0, 3)
                    ))
                    .map(Element::nextElementSibling)
                    .filter(Objects::nonNull)
                    .flatMap(first ->
                        first.classNames()
                            .stream()
                            .filter(className -> className.startsWith("predmety_vse_"))
                            .findFirst()
                            .map(className -> document.select("." + className)
                                .stream()
                                .map(row -> {
                                    final String code = Objects.requireNonNull(row.child(0)).text().trim();
                                    final String name = Objects.requireNonNull(row.child(1)).text().trim();

                                    return new InsisSubject(code, name);
                                })
                            )
                            .orElse(Stream.empty())
                    )
                    .toList();

                logger.info("Found {} subjects.", subjects.size());

                return Flux.fromIterable(subjects);
            });
    }

    // I hate InSIS so fucking much
    private String expandInsisUrl(final @NonNull String url) {
        // https://insis.vse.cz/katalog/plany.pl?...
        if (url.startsWith("https://")) {
            return url;
        }

        // /katalog/plany.pl?...
        if (url.startsWith("/")) {
            return INSIS_BASE_URL + url;
        }

        // /../katalog/syllabus.pl?...
        if (url.startsWith("../")) {
            return INSIS_BASE_URL + "/katalog/" + url;
        }

        // plany.pl?...
        return INSIS_BASE_URL + "/katalog/" + url;
    }
}
