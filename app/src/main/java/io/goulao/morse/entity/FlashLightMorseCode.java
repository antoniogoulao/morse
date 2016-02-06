package io.goulao.morse.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by NB21761 on 06/02/2016.
 */
public class FlashLightMorseCode {
    private static Map<String, Integer> flashLightValues = new HashMap<String, Integer>();
    private static Map<String, MorseCodeCharacter> charactersCache = new HashMap<String, MorseCodeCharacter>();

    static {
        flashLightValues.put(".", 150); // 1 time unit
        flashLightValues.put("-", 450); // 3 time units
    }

    public static Map<String, MorseCodeCharacter> getCharactersCache() {
        return charactersCache;
    }

    public static void setCharactersCache(Map<String, MorseCodeCharacter> charactersCache) {
        FlashLightMorseCode.charactersCache = charactersCache;
    }

    public static Map<String, Integer> getFlashLightValues() {
        return flashLightValues;
    }
    public static void setFlashLightValues(Map<String, Integer> flashLightValues) {
        FlashLightMorseCode.flashLightValues = flashLightValues;
    }
}
