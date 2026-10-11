package git.prayoadmii.velobrand;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import org.slf4j.Logger;

@Plugin(
  id = PluginMetadata.ID,
  name = PluginMetadata.NAME,
  version = PluginMetadata.VERSION,
  description = PluginMetadata.DESCRIPTION
)
public final class VeloBrandChanger {
  private final ProxyServer proxyServer;
  private final Logger logger;
  private final ConfigManager configManager;

  @Inject
  public VeloBrandChanger(
      ProxyServer proxyServer,
      Logger logger,
      @DataDirectory Path dataDirectory,
      @Named(PluginMetadata.ID) PluginContainer pluginContainer
  ) {
    this.proxyServer = proxyServer;
    this.logger = logger;
    this.configManager = new ConfigManager(dataDirectory);

    try {
      configManager.load();
    } catch (IOException exception) {
      throw new UncheckedIOException("Unable To Load Plugin Configuration!", exception);
    }

    proxyServer.getEventManager().register(pluginContainer, this);
    proxyServer.getEventManager().register(pluginContainer, new PingBrandListener(configManager));
    logger.info("Server-List Ping Brand Set To '{}'!", configManager.getBrand());
  }

  @Subscribe
  public void onProxyInitialize(ProxyInitializeEvent event) {
    CommandManager commandManager = proxyServer.getCommandManager();

    commandManager.register(commandManager.metaBuilder("vbrandchange").plugin(this).build(), new BrandChangeCommand(configManager, logger));
  }
}
