package cz.vse.fis.discord.insis;

import io.micronaut.core.annotation.NonNull;

public record InsisSubject(
    @NonNull String name,
    @NonNull String code
) {
}
