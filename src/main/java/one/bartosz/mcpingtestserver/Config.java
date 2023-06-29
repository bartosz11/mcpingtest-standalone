package one.bartosz.mcpingtestserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Objects;
import java.util.Properties;

public class Config {

    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
    private int port;
    private String address;
    private String motd;
    private int maxPlayers;
    private int slpOnlineCount;
    private boolean slpRealOnlineCount;
    private int slpMaxPlayerCount;
    private boolean slpRealMaxPlayerCount;
    private int slpProtocolVersion;
    private String slpVersion;
    private boolean slpHidePlayers;
    private String serverFullMessage;
    private String onJoinMessage;
    private boolean onlineMode;
    private boolean enableVelocitySupport;
    private String velocitySecret;
    private boolean enableBungeeCordSupport;
    private String bungeeGuardSecrets;
    private boolean enableBungeeGuard;
    private boolean isProxyEnabled;
    public Config() {
        if (!new File("config.properties").exists()) saveDefaultConfig();
        try {
            Properties props = new Properties();
            FileReader reader = new FileReader("config.properties");
            props.load(reader);
            this.port = Integer.parseInt(props.getProperty("server-port"));
            this.address = props.getProperty("server-ip");
            this.maxPlayers = Integer.parseInt(props.getProperty("max-players"));
            this.motd = props.getProperty("slp-motd");
            //SLP online player count and if it's real one or not
            String slpOnlinePlayerCount = props.getProperty("slp-onlineplayercount");
            if (slpOnlinePlayerCount.equalsIgnoreCase("real")) {
                this.slpOnlineCount = 0;
                this.slpRealOnlineCount = true;
            } else {
                this.slpOnlineCount = Integer.parseInt(slpOnlinePlayerCount);
                this.slpRealOnlineCount = false;
            }
            //SLP max player count and if it's real one or not
            String slpMaxPlayerCount = props.getProperty("slp-maxplayercount");
            if (slpMaxPlayerCount.equalsIgnoreCase("real")) {
                this.slpMaxPlayerCount = 0;
                this.slpRealMaxPlayerCount = true;
            } else {
                this.slpMaxPlayerCount = Integer.parseInt(slpOnlinePlayerCount);
                this.slpRealMaxPlayerCount = false;
            }
            this.slpProtocolVersion = Integer.parseInt(props.getProperty("slp-protocolversion"));
            this.slpVersion = props.getProperty("slp-version");
            this.slpHidePlayers = Boolean.parseBoolean(props.getProperty("slp-hideplayers"));
            this.serverFullMessage = props.getProperty("server-full-message");
            this.onJoinMessage = props.getProperty("on-join-message");
            this.onlineMode = Boolean.parseBoolean(props.getProperty("online-mode"));
            this.enableVelocitySupport = Boolean.parseBoolean(props.getProperty("velocity-support-enabled"));
            this.enableBungeeCordSupport = Boolean.parseBoolean(props.getProperty("bungeecord-support-enabled"));
            //lol
            if (enableBungeeCordSupport && enableVelocitySupport) {
                LOGGER.error("Velocity support and BungeeCord support can't be enabled at the same time! Choose one!");
                System.exit(2);
            }
            this.enableBungeeGuard = Boolean.parseBoolean(props.getProperty("bungeeguard-enabled"));
            this.bungeeGuardSecrets = props.getProperty("bungeeguard-secrets");
            this.velocitySecret = props.getProperty("velocity-secret");
            this.isProxyEnabled = enableBungeeCordSupport || enableVelocitySupport;
            LOGGER.info("Successfully Read settings from config.properties!");
        } catch (IOException e) {
            LOGGER.error("Failed reading settings from config.properties!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void saveDefaultConfig() {
        try {
            BufferedInputStream bis = new BufferedInputStream(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("default-config.properties")));
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("config.properties"));
            int read;
            while ((read = bis.read()) != -1) {
                bos.write(read);
            }
            bis.close();
            bos.close();
        } catch (IOException e) {
            LOGGER.error("Failed saving default config to config.properties!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }

    public String getMotd() {
        return motd;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getSlpOnlineCount() {
        return slpOnlineCount;
    }

    public boolean isSlpRealOnlineCount() {
        return slpRealOnlineCount;
    }

    public int getSlpMaxPlayerCount() {
        return slpMaxPlayerCount;
    }

    public boolean isSlpRealMaxPlayerCount() {
        return slpRealMaxPlayerCount;
    }

    public int getSlpProtocolVersion() {
        return slpProtocolVersion;
    }

    public String getSlpVersion() {
        return slpVersion;
    }

    public boolean isSlpHidePlayers() {
        return slpHidePlayers;
    }

    public String getServerFullMessage() {
        return serverFullMessage;
    }

    public String getOnJoinMessage() {
        return onJoinMessage;
    }

    public boolean isOnlineMode() {
        return onlineMode;
    }

    public boolean isEnableVelocitySupport() {
        return enableVelocitySupport;
    }

    public String getVelocitySecret() {
        return velocitySecret;
    }

    public boolean isEnableBungeeCordSupport() {
        return enableBungeeCordSupport;
    }

    public String getBungeeGuardSecrets() {
        return bungeeGuardSecrets;
    }

    public boolean isProxyEnabled() {
        return isProxyEnabled;
    }

    public boolean isEnableBungeeGuard() {
        return enableBungeeGuard;
    }
}
