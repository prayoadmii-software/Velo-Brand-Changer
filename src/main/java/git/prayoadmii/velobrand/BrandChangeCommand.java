package git.prayoadmii.velobrand;

import com.velocitypowered.api.command.SimpleCommand;
import java.io.IOException;
import org.slf4j.Logger;

public final class BrandChangeCommand implements SimpleCommand {
  private static final String RELOAD_PERMISSION = PluginMetadata.ID + ".reload";

  private final ConfigManager configManager;
  private final Logger logger;

  public BrandChangeCommand(ConfigManager configManager, Logger logger) {
    this.configManager = configManager;
    this.logger = logger;
  }

  @Override
  public void execute(Invocation invocation) {
    String[] arguments = invocation.arguments();

    if (arguments.length != 1 || !arguments[0].equalsIgnoreCase("reload")) {
      invocation.source().sendPlainMessage("Command Usage: /vbrandchange reload");
      return;
    }

    try {
      String reloadedBrand = configManager.load();

      logger.info("Server-List Ping Brand Reloaded To '{}'!", reloadedBrand);

      invocation.source().sendPlainMessage("Server-List Ping Brand Reloaded To '" + reloadedBrand + "'!");
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
