package cz.vse.fis.discord.bot.actions;

import cz.vse.fis.discord.bot.DiscordSubjectChannel;
import cz.vse.fis.discord.insis.InsisSubject;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
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

        final List<? extends ChannelAction> createActions = resolveCreateActions(channels, index);
        final List<? extends ChannelAction> renameActions = resolveRenameActions(channels, index);
        final List<? extends ChannelAction> combined =
            Stream.of(createActions, renameActions)
                .flatMap(Collection::stream)
                .toList();

        return Flux.fromIterable(combined);
    }

    private List<CreateChannelAction> resolveCreateActions(
        @NonNull Set<DiscordSubjectChannel> channels,
        @NonNull Map<String, InsisSubject> subjects
    ) {
        final Set<String> channelCodes = channels.stream()
            .map(channel -> channel.code().toLowerCase())
            .collect(Collectors.toSet());

        return subjects.values()
            .stream()
            .filter(subject -> subject.code().toLowerCase().matches("^[a-z0-9]{6}$"))
            .filter(subject -> !channelCodes.contains(subject.code().toLowerCase()))
            .map(subject -> new CreateChannelAction(
                buildSubjectChannelName(subject),
                subject.code(),
                subject.name()
            ))
            .toList();
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
                final String actualName = (subject.code() + "-" + channel.name());
                final String expectedName = buildSubjectChannelName(subject);

                return new RenameChannelAction(
                    actualName,
                    expectedName,
                    channel.id()
                );
            })
            .filter(action -> !action.oldName().equals(action.newName()))
            .toList();
    }

    @NonNull
    private static String buildSubjectChannelName(@NonNull InsisSubject subject) {
        return (subject.code() + "-" + subject.name())
            .toLowerCase()
            .replaceAll("[^a-z0-9ěščřžýáíéúů]", "-")
            .replaceAll("-{2,}", "-")
            .replaceAll("^-+", "")
            .replaceAll("-+$", "");
    }
}
