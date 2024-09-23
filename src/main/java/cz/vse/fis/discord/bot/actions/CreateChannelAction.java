package cz.vse.fis.discord.bot.actions;

import cz.vse.fis.discord.bot.DiscordBotConfiguration;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.spec.TextChannelCreateSpec;
import io.micronaut.core.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public record CreateChannelAction(
    @NonNull String name,
    @NonNull String code,
    @NonNull String subject
) implements ChannelAction {

    private static final Logger logger = LoggerFactory.getLogger(CreateChannelAction.class);

    @NonNull
    @Override
    public Mono<Void> perform(
        final @NonNull Guild guild,
        final @NonNull GatewayDiscordClient client,
        final @NonNull DiscordBotConfiguration configuration
    ) {
        logger.info("Creating channel #{}", name);

        return guild.createTextChannel(
            TextChannelCreateSpec.builder()
                .name(name)
                .topic(code.toUpperCase() + " - " + subject)
                .nsfw(false)
                .build()
        ).then();
    }
}
