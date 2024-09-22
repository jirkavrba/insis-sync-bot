package cz.vse.fis.discord.actions;

import io.micronaut.core.annotation.NonNull;

public interface ChannelAction {
    @NonNull
    String getAuditLogLine();
}
