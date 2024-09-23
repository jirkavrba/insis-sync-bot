package cz.vse.fis.discord.bot;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.TextChannel;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

@Singleton
@RequiredArgsConstructor
public class DiscordChannelService {

    private final Logger logger = LoggerFactory.getLogger(DiscordChannelService.class);

    private static final String SUBJECT_CHANNEL_PATTERN = "^[a-z0-9]{6}-.*";

    @NonNull
    public Mono<Set<DiscordSubjectChannel>> fetchSubjectChannels(final @NonNull Guild guild) {
        return guild.getChannels()
            .filter(channel -> channel instanceof TextChannel)
            .filter(channel -> channel.getName().matches(SUBJECT_CHANNEL_PATTERN))
            .map(channel -> {
                final String code = channel.getName().substring(0, 6);
                final String name = channel.getName().substring(7);

                return new DiscordSubjectChannel(code, name, channel.getId().asString());
            })
            .collectList()
            .map(HashSet::new);
    }

}
