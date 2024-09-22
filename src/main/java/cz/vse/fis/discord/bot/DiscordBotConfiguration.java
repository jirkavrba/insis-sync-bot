package cz.vse.fis.discord.bot;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.NonNull;

@ConfigurationProperties("discord")
public record DiscordBotConfiguration(
    @NonNull String token,
    @NonNull String guild
) {
    public DiscordBotConfiguration {
        if (token.isBlank()) throw new IllegalArgumentException("Discord token cannot be blank!");
        if (guild.isBlank()) throw new IllegalArgumentException("Discord guild ID cannot be blank!");
    }
}
