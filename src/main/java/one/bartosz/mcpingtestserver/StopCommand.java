package one.bartosz.mcpingtestserver;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//pterodactyl panel compatibility go brr
public class StopCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(StopCommand.class);

    public StopCommand() {
        super("stop", "end", "exit", "shutdown");
        setCondition((sender, commandString) -> sender.hasPermission("mcpingtest.stop") || sender instanceof ConsoleSender);
        setDefaultExecutor(((sender, context) -> {
            String commandName = context.getCommandName();
            String senderName = sender instanceof Player player ? player.getUsername() : "CONSOLE";
            LOGGER.info("Stopping server - command {} called by {}", commandName, senderName);
            MinecraftServer.stopCleanly();
        }));
    }
}
