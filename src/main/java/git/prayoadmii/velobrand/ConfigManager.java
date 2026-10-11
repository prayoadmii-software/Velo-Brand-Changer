package git.prayoadmii.velobrand;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

public final class ConfigManager {
  private static final String DEFAULT_BRAND = "&bVelocity&r";
  private static final Pattern YAML_SETTING = Pattern.compile("^([A-Za-z0-9_-]+):[ \\t]*(.*)$");
  private static final Pattern LEGACY_FORMATTING_CODE = Pattern.compile("&([0-9a-fk-or])", Pattern.CASE_INSENSITIVE);

  private final Path dataDirectory;
  private volatile String brand;

  public ConfigManager(Path dataDirectory) {
    this.dataDirectory = dataDirectory;
  }

  public String getBrand() {
    return brand;
  }

  public String load() throws IOException {
    String bundledConfig;

    try (var resource = ConfigManager.class.getResourceAsStream("/config.yml")) {
      if (resource == null) {
        throw new IOException("Bundled Default config.yml Resource Is Missing!");
      }

      bundledConfig = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
    }

    return load(bundledConfig);
  }

  private String load(String bundledConfig) throws IOException {
    Path configFile = dataDirectory.resolve("config.yml");
    Files.createDirectories(dataDirectory);

    Map<String, Object> oldSettings = new LinkedHashMap<>();

    if (Files.exists(configFile)) {
      oldSettings.putAll(loadYamlSettings(Files.readString(configFile, StandardCharsets.UTF_8), configFile));
    } else {
      importLegacyProperties(oldSettings);
    }

    Object existingBrand = oldSettings.get("brand");

    if (existingBrand == null || existingBrand instanceof String brandValue && brandValue.isBlank()) {
      oldSettings.put("brand", DEFAULT_BRAND);
    }

    String migratedConfig = migrateConfig(bundledConfig, oldSettings);
    Map<String, Object> config = loadYamlSettings(migratedConfig, configFile);
    Object configuredBrand = config.get("brand");

    if (!(configuredBrand instanceof String brandValue)) {
      throw new IllegalArgumentException("The 'brand' Setting In " + configFile + " Must Be A String!");
    }

    Files.writeString(configFile, migratedConfig, StandardCharsets.UTF_8);

    brand = convertLegacyFormatting(brandValue);

    return brand;
  }

  private void importLegacyProperties(Map<String, Object> oldSettings) throws IOException {
    Path legacyConfig = dataDirectory.resolve("config.properties");
    
    if (!Files.exists(legacyConfig)) {
      return;
    }

    Properties properties = new Properties();

    try (var reader = Files.newBufferedReader(legacyConfig, StandardCharsets.UTF_8)) {
      properties.load(reader);
    }

    String legacyBrand = properties.getProperty("brand");

    if (legacyBrand != null) {
      oldSettings.put("brand", legacyBrand);
    }
  }

  private static String convertLegacyFormatting(String value) {
    return LEGACY_FORMATTING_CODE.matcher(value).replaceAll("§$1");
  }

  private static Map<String, Object> loadYamlSettings(String yaml, Path configFile)
    throws IOException {
      LoaderOptions options = new LoaderOptions();

      options.setAllowDuplicateKeys(false);

      Object loaded;
    
    try {
      loaded = new Yaml(new SafeConstructor(options)).load(yaml);
    } catch (YAMLException exception) {
      throw new IOException("Invalid YAML in " + configFile, exception);
    }

    if (!(loaded instanceof Map<?, ?> settings)) {
      throw new IOException("Expected a YAML mapping in " + configFile);
    }

    Map<String, Object> result = new LinkedHashMap<>();

    for (Map.Entry<?, ?> setting : settings.entrySet()) {
      if (!(setting.getKey() instanceof String key)) {
        throw new IOException("Expected string setting names in " + configFile);
      }
      
      result.put(key, setting.getValue());
    }

    return result;
  }

  private static String migrateConfig(String bundledConfig, Map<String, Object> oldSettings) {
    StringBuilder migrated = new StringBuilder();
    Map<String, Boolean> bundledKeys = new LinkedHashMap<>();

    for (String line : bundledConfig.split("\\n", -1)) {
      Matcher matcher = YAML_SETTING.matcher(line);

      if (matcher.matches()) {
        String key = matcher.group(1);
        bundledKeys.put(key, true);
        Object oldValue = oldSettings.get(key);

        if (!key.equals("cfg-ver") && oldSettings.containsKey(key)) {
          validateScalar(key, oldValue);

          migrated.append(key).append(": ").append(serializeYamlScalar(oldValue));
        } else {
          migrated.append(line);
        }
      } else {
        migrated.append(line);
      }

      migrated.append('\n');
    }

    oldSettings.forEach((key, value) -> {
      if (!key.equals("cfg-ver") && !bundledKeys.containsKey(key)) {
        validateScalar(key, value);

        migrated.append("# Preserved Setting Not Present In The Bundled Defaults!\n")
            .append(key)
            .append(": ")
            .append(serializeYamlScalar(value))
            .append('\n');
      }
    });

    return migrated.toString();
  }

  private static void validateScalar(String key, Object value) {
    if (!(value instanceof String
        || value instanceof Number
        || value instanceof Boolean
        || value == null)) {
      throw new IllegalArgumentException("Cannot Migrate Non-Scalar Setting '" + key + "' From The Existing Config!");
    }
  }

  private static String serializeYamlScalar(Object value) {
    if (value == null) {
      return "null";
    }

    if (value instanceof Boolean || value instanceof Number) {
      return value.toString();
    }

    StringBuilder escaped = new StringBuilder("\"");

    for (int i = 0; i < ((String) value).length(); i++) {
      char character = ((String) value).charAt(i);

      switch (character) {
        case '\\' -> escaped.append("\\\\");
        case '"' -> escaped.append("\\\"");
        case '\n' -> escaped.append("\\n");
        case '\r' -> escaped.append("\\r");
        case '\t' -> escaped.append("\\t");
        case '\b' -> escaped.append("\\b");
        case '\f' -> escaped.append("\\f");

        default -> {
          if (Character.isISOControl(character)) {
            escaped.append(String.format("\\u%04X", (int) character));
          } else {
            escaped.append(character);
          }
        }
      }
    }

    return escaped.append('"').toString();
  }
}
