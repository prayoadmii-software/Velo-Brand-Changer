package git.prayoadmii.velobrand;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;

public final class PingBrandListener {
  private static final String BRAND_SUFFIX = "\u00a7r \u00a7b(Velocity)\u00a7r";

  private final ConfigManager configManager;

  public PingBrandListener(ConfigManager configManager) {
    this.configManager = configManager;
  }

  @Subscribe
  public void onProxyPing(ProxyPingEvent event) {
    ServerPing ping = event.getPing();
    ServerPing.Version currentVersion = ping.getVersion();
    String brand = configManager.getBrand() + BRAND_SUFFIX;

    event.setPing(ping.asBuilder().version(new ServerPing.Version(currentVersion.getProtocol(), brand)).build());
  }
}
