package nl.tijsbeek.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * Contains the config's variables
 */
public class Config {

    private final String discordToken;
    private final int prometheusPort;
    private final String databasePort;
    private final String databaseUsername;
    private final String databasePassword;

    /**
     * Creates an instance
     *
     * @param discordToken the discord-token
     * @param prometheusPort the port to run prometheus on
     * @param databasePort the port of the DB
     * @param databaseUsername the username of the DB
     * @param databasePassword the password of the DB
     */
    @JsonCreator
    @Contract(pure = true)
    public Config(@JsonProperty("discord_token") String discordToken,
                  @JsonProperty("prometheus_port") String prometheusPort,
                  @JsonProperty("database_port") String databasePort,
                  @JsonProperty("database_username") String databaseUsername,
                  @JsonProperty("database_password") String databasePassword) {

        this.discordToken = discordToken;
        this.prometheusPort = Integer.parseInt(prometheusPort);
        this.databasePort = databasePort;
        this.databaseUsername = databaseUsername;
        this.databasePassword = databasePassword;
    }

    /**
     * The Discord token
     * @return the Discord token
     */
    public String getDiscordToken() {
        return discordToken;
    }

    /**
     * the port to run prometheus on
     * @return the port to run prometheus on
     */
    public int getPrometheusPort() {
        return prometheusPort;
    }

    /**
     * The port of the DB
     * @return the port of the DB
     */
    public String getDatabasePort() {
        return databasePort;
    }

    /**
     * The username of the DB
     * @return the username of the DB
     */
    public String getDatabaseUsername() {
        return databaseUsername;
    }

    /**
     * The password of the DB
     * @return the password of the DB
     */
    public String getDatabasePassword() {
        return databasePassword;
    }

    /**
     * Creates an instance based of the URL of the file.
     *
     * @param configUrl a valid URL
     *
     * @return the Config
     *
     * @throws IOException if a low-level I/O error occurs.
     */
    @NotNull
    public static Config loadInstance(@NotNull final String configUrl) throws IOException {
        return new ObjectMapper().readValue(new File(configUrl), Config.class);
    }
}