package cz.vse.fis.discord.bot;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

@ConfigurationProperties("discord")
public record DiscordBotConfiguration(
    @NonNull String token,
    @NonNull String guild,
    @NonNull String studentRole
) {
    public DiscordBotConfiguration {
        validate(token, "Discord token");
        validate(guild, "Discord guild ID");
        validate(studentRole, "Student role ID");
    }

    private static void validate(@Nullable String value, @NonNull String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank!");
        }
    }
}
