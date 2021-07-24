package ru.ruscalworld.pollbot.core.i18n;

import ru.ruscalworld.pollbot.PollBot;

import java.util.HashMap;

public class Translator {
    public static final String DEFAULT_LANGUAGE = "en";

    public static String translate(String language, String key, Object... args) {
        HashMap<String, Translation> translations = PollBot.getInstance().getTranslations();
        Translation fallbackTranslation = translations.get(DEFAULT_LANGUAGE);
        Translation translation = translations.getOrDefault(language, fallbackTranslation);
        String text = translation.getProperties().getProperty(key, fallbackTranslation.getProperties().getProperty(key));
        if (text == null) throw new IllegalArgumentException("Translation for key \"" + key + "\" cannot be found");
        return String.format(text, args);
    }
}
