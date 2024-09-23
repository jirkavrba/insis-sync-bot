package cz.vse.fis.discord.bot.actions;

import cz.vse.fis.discord.bot.DiscordBotConfiguration;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.TextChannelEditSpec;
import io.micronaut.core.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public final class OrganizeChannelsAction implements ChannelAction {

    private static final Logger logger = LoggerFactory.getLogger(OrganizeChannelsAction.class);

    private static final OrganizeChannelsAction INSTANCE = new OrganizeChannelsAction();

    private OrganizeChannelsAction() {
    }

    @NonNull
    public static OrganizeChannelsAction getInstance() {
        return INSTANCE;
    }

    @Override
    public Mono<Void> perform(
        final @NonNull Guild guild,
        final @NonNull GatewayDiscordClient client,
        final @NonNull DiscordBotConfiguration configuration
    ) {
        logger.info("Organizing channels by their name.");

        return guild.getChannels()
            .filter(channel -> channel instanceof TextChannel)
            .cast(TextChannel.class)
            .collectList()
            .flatMap(channels -> {
                final List<TextChannel> sortedSubjectChannels = channels.stream()
                    .filter(channel -> channel.getName().matches("^[a-z0-9]{6}-.*"))
                    .sorted(Comparator.comparing(TextChannel::getName))
                    .toList();

                final List<Mono<Void>> actions = IntStream.range(0, sortedSubjectChannels.size())
                    .mapToObj(index ->
                        sortedSubjectChannels
                            .get(index)
                            .edit(TextChannelEditSpec.builder()
                                .position(channels.size() + index)
                                .build())
                            .then()
                    ).toList();

                return Mono.when(actions);
            });
    }
}
