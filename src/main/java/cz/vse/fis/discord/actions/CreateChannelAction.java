package cz.vse.fis.discord.actions;

import io.micronaut.core.annotation.NonNull;

public record CreateChannelAction(
    @NonNull String name
) implements ChannelAction {

    @NonNull
    @Override
    public String getAuditLogLine() {
        return String.format("\uD83D\uDCDD Create a new channel `%s`", name);
    }
}
