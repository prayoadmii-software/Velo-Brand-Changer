# Velo Brand Changer

A Velocity 4 plugin that changes the version/brand text shown in the Minecraft
server-list ping. It changes only the displayed text; the original protocol
version and the rest of the ping response are preserved.

## Build

Requires JDK 25 or newer and Gradle 9.1 or newer:

```sh
./gradlew build
```

The plugin jar is written to `build/libs/velobrandchanger-1.0.0.jar`.
The project group, artifact ID, plugin ID/name/version/description, Velocity API
version, Java compatibility, and Java bytecode target are configured in
`gradle.properties`. The plugin targets Java 21 bytecode while resolving the
Velocity 4.2 API for Java 25 compatibility. The Java package remains defined by
the source layout.

## Code layout

- `VeloBrandChanger` wires the plugin and registers its components.
- `ConfigManager` loads, migrates, and reloads `config.yml`.
- `PingBrandListener` applies the configured brand to server-list pings.
- `BrandChangeCommand` implements `/vbrandchange reload`.

## Install and configure

Copy the jar into the Velocity proxy's `plugins` directory and start the proxy.
The plugin installs `src/main/resources/config.yml` as
`plugins/velobrandchanger/config.yml`. On every start (and reload), it migrates
the active file to the latest bundled settings and comments while preserving
existing scalar setting values. The internal `cfg-ver` value is updated from
the bundled template. If only the old `config.properties` exists, its `brand`
value is imported into the new YAML config.

```yaml
brand: "&bVelocity Proxy&r"
cfg-ver: 1
```

Set `brand` to the text you want shown as the server version in the ping.
Formatting uses classic Minecraft codes with `&` (for example `&c&lExample&r`);
these are converted to section-sign codes for the ping response. Apply changes
immediately with `/vbrandchange reload`.

The reload command requires the `velo-brand-changer.reload` permission.
