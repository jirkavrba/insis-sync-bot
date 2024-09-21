package cz.vse.fis.discord;

import cz.vse.fis.discord.insis.InsisScrapingService;
import io.micronaut.configuration.picocli.PicocliRunner;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;

import java.util.Objects;

@Command(name = "insis-sync-bot", description = "...", mixinStandardHelpOptions = true)
public class InsisSyncBotCommand implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(InsisSyncBotCommand.class);

    @Inject
    private InsisScrapingService service;

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(InsisSyncBotCommand.class, args);
    }

    public void run() {
        logger.info("Starting the InSIS sync bot.");

        final var subjects = Objects.requireNonNull(service.scrapeAvailableSubjects().block());

        logger.info("Found a total of {} unique subjects that will be synced", subjects.size());
        logger.info("Subjects were synced successfully.");
    }
}
