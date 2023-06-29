package one.bartosz.mcpingtestserver;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.*;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.client.play.ClientKeepAlivePacket;
import net.minestom.server.network.packet.server.play.KeepAlivePacket;
import net.minestom.server.ping.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class McPingTestServer {

    private static final TextColor mainColor = TextColor.fromHexString("#089103");
    private static final TextColor accentColor = TextColor.fromHexString("#db5202");
    private static final Logger LOGGER = LoggerFactory.getLogger(McPingTestServer.class);
    public static void main(String[] args) {
        Config cfg = new Config();
        HashMap<Player, List<Integer>> playerPings = new HashMap<>();
        HashMap<Player, Long> keepAlive = new HashMap<>();
        HashMap<Player, BossBar> bossBars = new HashMap<>();
        //init
        MinecraftServer minecraftServer = MinecraftServer.init();
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        //create a world (?)
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
        //set time to night, stop time from passing
        instanceContainer.setTime(18000);
        instanceContainer.setTimeRate(0);
        //blank chunks generator - results in a "limbo" server
        instanceContainer.setGenerator(unit ->
                unit.modifier().fillHeight(0, 0, Block.AIR));
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
        // allow players to join
        eventHandler.addListener(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 100, 0));
            //add them to the hashmap since they can send keep alive packets already
            playerPings.put(event.getPlayer(), new ArrayList<>());
        });
        eventHandler.addListener(PlayerSpawnEvent.class, event -> {
            Player player = event.getPlayer();
            //stop players from moving in the Y axis, prevents them from dying from void
            player.setNoGravity(true);
            player.setFlying(true);
            String onJoinMessage = cfg.getOnJoinMessage();
            if (!onJoinMessage.equalsIgnoreCase("none")) player.sendMessage(Component.text(onJoinMessage).color(TextColor.fromHexString("#55FF55")));
        });
        //player limit handler
        eventHandler.addListener(AsyncPlayerPreLoginEvent.class, event -> {
            Player player = event.getPlayer();
            LOGGER.info("Player {} (UUID: {}) connected. (IP: {}})", player.getUsername(), player.getUuid(), player.getPlayerConnection().getRemoteAddress());
            int maxPlayers = cfg.getMaxPlayers();
            if (maxPlayers == -1) return;
            int onlinePlayers = instanceContainer.getPlayers().size();
            if (onlinePlayers >= maxPlayers) {
                player.kick(cfg.getServerFullMessage());
                LOGGER.info("Player {} (UUID: {}) kicked: server is full!", player.getUsername(), player.getUuid());
            }
        });
        //no point in keeping the data if player left
        eventHandler.addListener(PlayerDisconnectEvent.class, event -> {
            Player player = event.getPlayer();
            System.out.printf("Player %s (UUID: %s) disconnected.", player.getUsername(), player.getUuid());
            LOGGER.info("Player {} (UUID: {}) disconnected.", player.getUsername(), player.getUuid());
            playerPings.remove(player);
            keepAlive.remove(player);
        });
        //packet listeners
        eventHandler.addListener(PlayerPacketOutEvent.class, event -> {
            if (event.getPacket() instanceof KeepAlivePacket)
                keepAlive.put(event.getPlayer(), System.currentTimeMillis());
        });
        eventHandler.addListener(PlayerPacketEvent.class, event -> {
            if (event.getPacket() instanceof ClientKeepAlivePacket packet) {
                Player player = event.getPlayer();
                int ping = (int) (keepAlive.get(player) - packet.id());
                playerPings.get(player).add(ping);
                int measurements = playerPings.get(player).size();
                int avg = playerPings.get(player).stream().reduce(0, Integer::sum) / measurements;
                //curent ping: <ping>
                TextComponent msg = Component.text("Current ping: ").color(mainColor).append(Component.text(ping).color(accentColor))
                        //ms | average: <avg>
                        .append(Component.text(" ms | Average: ").color(mainColor)).append(Component.text(avg).color(accentColor))
                        //ms | Measurements: <x>
                        .append(Component.text(" ms | Measurements: ").color(mainColor)).append(Component.text(measurements).color(accentColor));
                BossBar bossBar = BossBar.bossBar(msg, 1.0F, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
                if (bossBars.containsKey(player)) player.hideBossBar(bossBars.get(player));
                bossBars.put(player, bossBar);
                player.showBossBar(bossBar);
            }
        });
        eventHandler.addListener(ServerListPingEvent.class, serverListPingEvent -> {
            ResponseData respData = new ResponseData();
            respData.setDescription(LegacyComponentSerializer.legacyAmpersand().deserialize(cfg.getMotd()));
            if (cfg.isSlpRealMaxPlayerCount()) respData.setMaxPlayer(cfg.getMaxPlayers());
            else respData.setMaxPlayer(cfg.getSlpMaxPlayerCount());
            if (cfg.isSlpRealOnlineCount()) respData.setOnline(instanceContainer.getPlayers().size());
            else respData.setOnline(cfg.getSlpOnlineCount());
            respData.setPlayersHidden(cfg.isSlpHidePlayers());
            respData.setProtocol(cfg.getSlpProtocolVersion());
            respData.setVersion(cfg.getSlpVersion());
            //todo respData.setFavicon();
            serverListPingEvent.setResponseData(respData);
        });
        if (cfg.isOnlineMode() && !cfg.isProxyEnabled()) MojangAuth.init();
        if (cfg.isEnableVelocitySupport()) VelocityProxy.enable(cfg.getVelocitySecret());
        if (cfg.isEnableBungeeCordSupport()) {
            if (cfg.isEnableBungeeGuard()) {
                Set<String> bungeeGuardTokens = Arrays.stream(cfg.getBungeeGuardSecrets().split(",")).collect(Collectors.toSet());
                BungeeCordProxy.setBungeeGuardTokens(bungeeGuardTokens);
            }
            BungeeCordProxy.enable();
        }
        MinecraftServer.getCommandManager().register(new StopCommand());
        LOGGER.info("Starting server...");
        minecraftServer.start(cfg.getAddress(), cfg.getPort());
        //graceful shutdown on ctrl+c
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Stopping...");
            MinecraftServer.stopCleanly();
        }));
    }

}
