package cz.vse.fis.discord.bot;

import io.micronaut.core.annotation.NonNull;

public record DiscordSubjectChannel(
    @NonNull String code,
    @NonNull String name,
    @NonNull String id
) {
}
