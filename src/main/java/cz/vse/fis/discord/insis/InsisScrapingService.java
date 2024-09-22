package cz.vse.fis.discord.insis;

import io.micronaut.core.annotation.NonNull;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface InsisScrapingService {
    @NonNull
    Mono<Set<InsisSubject>> scrapeAvailableSubjects();
}
