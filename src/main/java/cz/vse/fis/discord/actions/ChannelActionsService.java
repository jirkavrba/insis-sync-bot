package cz.vse.fis.discord.actions;

import cz.vse.fis.discord.bot.DiscordSubjectChannel;
import cz.vse.fis.discord.insis.InsisSubject;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class ChannelActionsService {

    private final Logger logger = LoggerFactory.getLogger(ChannelActionsService.class);

    @NonNull
    public Flux<ChannelAction> resolveChannelActions(
        final @NonNull Set<DiscordSubjectChannel> channels,
        final @NonNull Set<InsisSubject> subjects
    ) {
        final Map<String, InsisSubject> index = subjects.stream()
            .collect(
                Collectors.toMap(
                    InsisSubject::code,
                    Function.identity()
                )
            );

        final List<? extends ChannelAction> renameActions = resolveRenameActions(channels, index);
        final List<? extends ChannelAction> combined =
            Stream.of(renameActions)
                .flatMap(Collection::stream)
                .toList();

        return Flux.fromIterable(combined);
    }

    @NonNull
    private List<RenameChannelAction> resolveRenameActions(
        @NonNull Set<DiscordSubjectChannel> channels,
        @NonNull Map<String, InsisSubject> subjects
    ) {
        return channels.stream()
            .filter(channel -> subjects.containsKey(channel.code().toLowerCase()))
            .map(channel -> {
                final InsisSubject subject = subjects.get(channel.code().toLowerCase());
                final String actualName = channel.name();
                final String expectedName = subject.name()
                    .toLowerCase()
                    .replaceAll("[^a-zěščřžýáíé]", "-")
                    .replaceAll("-{2,}", "-");

                return new RenameChannelAction(
                    actualName,
                    expectedName,
                    channel.id()
                );
            })
            .filter(action -> !action.oldName().equals(action.newName()))
            .toList();
    }
}
