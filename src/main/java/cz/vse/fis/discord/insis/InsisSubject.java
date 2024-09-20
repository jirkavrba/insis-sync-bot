package cz.vse.fis.discord.insis;

import io.micronaut.core.annotation.NonNull;

public record InsisSubject(
    @NonNull String code,
    @NonNull String name
) {
}
