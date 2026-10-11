# Velo Brand Changer

A Velocity 4 plugin that changes the version/brand text shown in the Minecraft
server-list ping. It changes only the displayed text; the original protocol
version and the rest of the ping response are preserved.

## Build

Requires JDK 25 or newer and Gradle 9.1 or newer:

```sh
./gradlew build
```

The plugin jar is written to `build/libs/velo-brand-changer-1.0.0.jar`.
The project group, artifact ID, plugin ID/name/version/description, Velocity API
version, Java compatibility, and Java bytecode target are configured in
`gradle.properties`. The plugin targets Java 21 bytecode while resolving the
Velocity 4.2 API for Java 25 compatibility. The Java package remains defined by
the source layout.

## Install and configure

Copy the jar into the Velocity proxy's `plugins` directory and start the proxy
once. The plugin creates `plugins/velo-brand-changer/config.properties` with:

```properties
brand=My Minecraft Server
```

Set `brand` to the text you want shown as the server version in the ping. Apply
changes immediately with `/vbrandchange reload`.

The `brand` value supports MiniMessage formatting, for example:

```properties
brand=<gradient:#00D1FF:#8A2BE2><bold>My Minecraft Server</bold></gradient>
```

Formatting is converted to legacy section-sign codes for the vanilla-compatible
ping version string.

The reload command requires the `velo-brand-changer.reload` permission.
