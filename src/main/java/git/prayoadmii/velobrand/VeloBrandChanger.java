package git.prayoadmii.velobrand;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;

@Plugin(
    id = PluginMetadata.ID,
    name = PluginMetadata.NAME,
    version = PluginMetadata.VERSION,
    description = PluginMetadata.DESCRIPTION
)
public final class VeloBrandChanger {
  private static final String DEFAULT_CONFIG = "brand=My Minecraft Server\n";
  private static final String RELOAD_PERMISSION = PluginMetadata.ID + ".reload";

  private final ProxyServer proxyServer;
  private final Logger logger;
  private final Path dataDirectory;
  private volatile String brand;

  @Inject
  public VeloBrandChanger(
      ProxyServer proxyServer,
      Logger logger,
      @DataDirectory Path dataDirectory,
      @Named(PluginMetadata.ID) PluginContainer pluginContainer
  ) {
    this.proxyServer = proxyServer;
    this.logger = logger;
    this.dataDirectory = dataDirectory;

    try {
      this.brand = loadBrand(dataDirectory);
    } catch (IOException exception) {
      throw new UncheckedIOException("Unable to load plugin configuration.", exception);
    }

    proxyServer.getEventManager().register(pluginContainer, this);
    logger.info("Server-list ping brand set to '{}'.", brand);
  }

  @Subscribe
  public void onProxyInitialize(ProxyInitializeEvent event) {
    CommandManager commandManager = proxyServer.getCommandManager();

    commandManager.register(commandManager.metaBuilder("vbrandchange").plugin(this).build(), new BrandChangeCommand());
  }

  @Subscribe
  public void onProxyPing(ProxyPingEvent event) {
    ServerPing ping = event.getPing();
    ServerPing.Version currentVersion = ping.getVersion();

    event.setPing(ping.asBuilder().version(new ServerPing.Version(currentVersion.getProtocol(), brand)).build());
  }

  private String reloadBrand() throws IOException {
    String loadedBrand = loadBrand(dataDirectory);

    brand = loadedBrand;

    return loadedBrand;
  }

  private static String loadBrand(Path dataDirectory) throws IOException {
    Path configFile = dataDirectory.resolve("config.properties");
    Files.createDirectories(dataDirectory);

    if (Files.notExists(configFile)) {
      Files.writeString(configFile, DEFAULT_CONFIG, StandardCharsets.UTF_8);
    }

    Properties config = new Properties();

    try (var reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
      config.load(reader);
    }

    String configuredBrand = config.getProperty("brand");
    if (configuredBrand == null || configuredBrand.isBlank()) {
      throw new IllegalArgumentException("The 'brand' Setting In " + configFile + " Must Not Be That Empty!");
    }

    return LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(configuredBrand));
  }

  private final class BrandChangeCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
      String[] arguments = invocation.arguments();

      if (arguments.length != 1 || !arguments[0].equalsIgnoreCase("reload")) {
        invocation.source().sendPlainMessage("Command Usage: /vbrandchange reload");

        return;
      }

      try {
        String reloadedBrand = reloadBrand();

        logger.info("Server-List Ping Brand Reloaded To '{}'!", reloadedBrand);

        invocation.source().sendPlainMessage("Server-list ping brand reloaded to '" + reloadedBrand + "'.");
      } catch (IOException | IllegalArgumentException exception) {
        logger.error("Unable To Reload The Server-List Ping Brand Configuration!", exception);

        invocation.source().sendPlainMessage("Failed To Reload The Brand Configuration! Check The Proxy Log For Details!");
      }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
      return invocation.source().hasPermission(RELOAD_PERMISSION);
    }
  }
}
