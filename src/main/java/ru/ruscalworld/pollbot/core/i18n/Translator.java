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

    public static String translate(String language, int number, String key, Object... args) {
        return getPlural(
                number,
                translate(language, key + ".1", args),
                translate(language, key + ".2", args),
                translate(language, key + ".5", args)
        );
    }

    public static String getPlural(int count, String one, String two, String five) {
        if (count % 10 == 1 && count % 100 != 11) {
            return one;
        } else if (count % 10 >= 2 && count % 10 <= 4 && (count % 100 < 10 || count % 100 >= 20)) return two;
        return five;
    }
}
