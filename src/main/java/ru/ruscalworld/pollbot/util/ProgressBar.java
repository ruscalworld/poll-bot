package ru.ruscalworld.pollbot.util;

public class ProgressBar {
    public static final String PB_START = "<:pb_start:777646453338275861>";
    public static final String PB_MIDDLE = "<:pb_middle:777646473369878528>";
    public static final String PB_END = "<:pb_end:777646488900599808>";
    public static final String PB_SINGLE = "<:pb_single:777646674981027891>";

    public static String makeDefault(int percentage, int length) {
        return make(percentage, length, PB_START, PB_MIDDLE, PB_END, PB_SINGLE);
    }

    public static String make(int percentage, int length, String firstChar, String middleChar, String lastChar, String single) {
        StringBuilder progress = new StringBuilder();
        for (int i = 0; i < length; i++) {
            float currentPercentage = (float) i / (float) length * 100;
            float nextPercentage = (float) (i + 1) / (float) length * 100;
            boolean isFirst = i == 0;
            boolean isLast = nextPercentage >= percentage;

            if (isFirst && isLast) return single;

            if (percentage > currentPercentage) {
                if (isFirst) {
                    progress.append(firstChar);
                } else if (isLast) {
                    progress.append(lastChar);
                    return progress.toString();
                } else progress.append(middleChar);
            }
        }
        return progress.toString();
    }
}
