package cz.vse.fis.discord.bot.actions;

import cz.vse.fis.discord.bot.DiscordBotConfiguration;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import io.micronaut.core.annotation.NonNull;
import reactor.core.publisher.Mono;

public interface ChannelAction {
    @NonNull
    Mono<Void> perform(
        @NonNull Guild guild,
        @NonNull GatewayDiscordClient client,
        @NonNull DiscordBotConfiguration configuration
    );
}
