package cz.vse.fis.discord;

import io.micronaut.configuration.picocli.PicocliRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;

@Command(name = "insis-sync-bot", description = "...", mixinStandardHelpOptions = true)
public class InsisSyncBotCommand implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(InsisSyncBotCommand.class);

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(InsisSyncBotCommand.class, args);
    }

    public void run() {
        logger.info("Starting the InSIS sync bot.");
    }
}
