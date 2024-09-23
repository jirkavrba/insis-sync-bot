package cz.vse.fis.discord.bot.actions;

import cz.vse.fis.discord.bot.DiscordBotConfiguration;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.TextChannelEditSpec;
import io.micronaut.core.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public record RenameChannelAction(
    @NonNull String oldName,
    @NonNull String newName,
    @NonNull String channelId
) implements ChannelAction {
    private static final Logger logger = LoggerFactory.getLogger(RenameChannelAction.class);

    @Override
    public Mono<Void> perform(
        final @NonNull Guild guild,
        final @NonNull GatewayDiscordClient client,
        final @NonNull DiscordBotConfiguration configuration
    ) {
        logger.info("Renaming channel from #{} to #{}", oldName, newName);

        return guild.getChannelById(Snowflake.of(channelId))
            .filter(channel -> channel instanceof TextChannel)
            .cast(TextChannel.class)
            .flatMap(channel ->
                channel.edit(
                    TextChannelEditSpec.builder()
                        .name(newName)
                        .build()
                )
            )
            .then();
    }
}
