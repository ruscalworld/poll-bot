package ru.ruscalworld.pollbot.util;

import com.vdurmont.emoji.EmojiParser;

public class Emoji {
    public static String getNumberEmoji(int number) {
        switch (number) {
            default:
            case 0:
                return EmojiParser.parseToUnicode(":zero:");
            case 1:
                return EmojiParser.parseToUnicode(":one:");
            case 2:
                return EmojiParser.parseToUnicode(":two:");
            case 3:
                return EmojiParser.parseToUnicode(":three:");
            case 4:
                return EmojiParser.parseToUnicode(":four:");
            case 5:
                return EmojiParser.parseToUnicode(":five:");
            case 6:
                return EmojiParser.parseToUnicode(":six:");
            case 7:
                return EmojiParser.parseToUnicode(":seven:");
            case 8:
                return EmojiParser.parseToUnicode(":eight:");
            case 9:
                return EmojiParser.parseToUnicode(":nine:");
            case 10:
                return EmojiParser.parseToUnicode(":keycap_ten:");
        }
    }
}
