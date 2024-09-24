package cz.vse.fis.discord.bot;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.TextChannel;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

import static cz.vse.fis.discord.bot.DiscordConstants.SUBJECT_CHANNEL_PATTERN;

@Singleton
@RequiredArgsConstructor
public class DiscordChannelService {

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
