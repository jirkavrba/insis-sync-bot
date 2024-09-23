package cz.vse.fis.discord.bot.actions;

import cz.vse.fis.discord.bot.DiscordBotConfiguration;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.TextChannelEditSpec;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import io.micronaut.core.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public final class SyncChannelPermissionsAction implements ChannelAction {

    private static final Logger logger = LoggerFactory.getLogger(SyncChannelPermissionsAction.class);

    private static final SyncChannelPermissionsAction INSTANCE = new SyncChannelPermissionsAction();

    private SyncChannelPermissionsAction() {
    }

    @NonNull
    public static SyncChannelPermissionsAction getInstance() {
        return INSTANCE;
    }

    @Override
    public Mono<Void> perform(
        final @NonNull Guild guild,
        final @NonNull GatewayDiscordClient client,
        final @NonNull DiscordBotConfiguration configuration
    ) {
        logger.info("Syncing channel permissions of all subject channels.");

        return guild.getEveryoneRole().flatMap(everyone ->
            guild.getChannels()
                .filter(channel -> channel instanceof TextChannel)
                .filter(channel -> channel.getName().matches("^[a-z0-9]{6}-.*"))
                .cast(TextChannel.class)
                .flatMap(channel -> {
                    logger.info("Syncing permissions for channel #{}", channel.getName());

                    return channel.edit(
                        TextChannelEditSpec.builder()
                            .parentIdOrNull(null)
                            .addPermissionOverwrite(
                                PermissionOverwrite.forRole(
                                    everyone.getId(),
                                    PermissionSet.none(),
                                    PermissionSet.all()
                                )
                            )
                            .addPermissionOverwrite(
                                PermissionOverwrite.forRole(
                                    Snowflake.of(configuration.studentRole()),
                                    PermissionSet.of(
                                        Permission.VIEW_CHANNEL,
                                        Permission.READ_MESSAGE_HISTORY,
                                        Permission.ADD_REACTIONS,
                                        Permission.ATTACH_FILES,
                                        Permission.SEND_MESSAGES,
                                        Permission.CREATE_PUBLIC_THREADS
                                    ),
                                    PermissionSet.none()
                                )
                            )
                            .parentId(Possible.absent())
                            .build()
                    );
                })
                .then()
        );
    }
}
