package ru.ruscalworld.pollbot.core.i18n;

import com.vdurmont.emoji.EmojiParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.ruscalworld.pollbot.PollBot;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Translation {
    private final String name;
    private final Properties properties;

    public Translation(String name, Properties properties) {
        this.name = name;
        this.properties = properties;
    }

    public static Translation load(String name) throws IOException {
        String fileName = "lang/" + name + ".properties";
        InputStream stream = PollBot.class.getClassLoader().getResourceAsStream(fileName);
        if (stream == null) throw new IllegalArgumentException("Translation at " + fileName + " could not be found");
        Properties properties = new Properties();
        properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        return new Translation(name, properties);
    }

    public @Nullable String getLocalizedName() {
        return this.getProperties().getProperty("translation.name");
    }

    public @NotNull String getEmoji() {
        return EmojiParser.parseToUnicode(this.getEmojiName());
    }

    public @NotNull String getEmojiName() {
        if ("en".equals(this.getName())) return ":gb:";
        return String.format(":%s:", this.getName());
    }

    public Properties getProperties() {
        return properties;
    }

    public String getName() {
        return name;
    }
}
