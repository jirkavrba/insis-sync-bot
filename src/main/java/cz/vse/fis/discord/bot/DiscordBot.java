package cz.vse.fis.discord.bot;

import cz.vse.fis.discord.bot.actions.ChannelActionsService;
import cz.vse.fis.discord.bot.actions.OrganizeChannelsAction;
import cz.vse.fis.discord.bot.actions.SyncChannelPermissionsAction;
import cz.vse.fis.discord.insis.InsisScrapingService;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@Singleton
@RequiredArgsConstructor
public class DiscordBot {

    private final Logger logger = LoggerFactory.getLogger(DiscordBot.class);

    @NonNull
    private final DiscordBotConfiguration configuration;

    @NonNull
    private final InsisScrapingService scraper;

    @NonNull
    private final DiscordChannelService channels;

    @NonNull
    private final ChannelActionsService actions;

    public Mono<Void> start() {
        logger.info("Starting the Discord bot");

        return DiscordClient
            .create(configuration.token())
            .withGateway(client -> client.on(ReadyEvent.class, event ->
                client.getGuildById(Snowflake.of(configuration.guild()))
                    .flatMapMany(guild ->
                        channels.fetchSubjectChannels(guild)
                            .zipWith(scraper.scrapeAvailableSubjects())
                            .flatMapMany(tuple -> actions.resolveChannelActions(
                                tuple.getT1(),
                                tuple.getT2()
                            ))
                            .concatWithValues(
                                SyncChannelPermissionsAction.getInstance(),
                                OrganizeChannelsAction.getInstance()
                            )
                            .flatMap(action -> action.perform(guild, client, configuration))
                    )
                    .then()))
            .doOnError(error -> logger.error("Error while running the Discord bot", error))
            .then();
    }
}
