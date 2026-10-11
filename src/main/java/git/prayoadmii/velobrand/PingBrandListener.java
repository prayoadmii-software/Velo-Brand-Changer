package git.prayoadmii.velobrand;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;

public final class PingBrandListener {
  private final ConfigManager configManager;

  public PingBrandListener(ConfigManager configManager) {
    this.configManager = configManager;
  }

  @Subscribe
  public void onProxyPing(ProxyPingEvent event) {
    ServerPing ping = event.getPing();
    ServerPing.Version currentVersion = ping.getVersion();

    event.setPing(ping.asBuilder().version(new ServerPing.Version(currentVersion.getProtocol(), configManager.getBrand())).build());
  }
}
