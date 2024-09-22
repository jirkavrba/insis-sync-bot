package cz.vse.fis.discord.actions;

import io.micronaut.core.annotation.NonNull;

public record RenameChannelAction(
    @NonNull String oldName,
    @NonNull String newName,
    @NonNull String channelId
) implements ChannelAction {

    @NonNull
    @Override
    public String getAuditLogLine() {
        return String.format("\uD83D\uDCDD Rename channel with ID %s from `%s` to `%s`.", channelId, oldName,  newName);
    }
}
