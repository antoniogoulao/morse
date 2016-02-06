package io.goulao.morse.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by NB21761 on 06/02/2016.
 */
public class MorseCodeLibrary {
    private static Map<String, String> morseCodeCharacterList = new HashMap<String, String>();;

    public static Map<String, String> getMorseCodeCharacterList() {
        return morseCodeCharacterList;
    }

    static {
        morseCodeCharacterList.put("a", ".-");
        morseCodeCharacterList.put("b", "-...");
        morseCodeCharacterList.put("c", "-.-.");
        morseCodeCharacterList.put("d", "-..");
        morseCodeCharacterList.put("e", ".");
        morseCodeCharacterList.put("f", "..-.");
        morseCodeCharacterList.put("g", "--.");
        morseCodeCharacterList.put("h", "....");
        morseCodeCharacterList.put("i", "..");
        morseCodeCharacterList.put("j", ".---");
        morseCodeCharacterList.put("k", "-.-");
        morseCodeCharacterList.put("l", ".-..");
        morseCodeCharacterList.put("m", "--");
        morseCodeCharacterList.put("n", "-.");
        morseCodeCharacterList.put("o", "---");
        morseCodeCharacterList.put("p", ".--.");
        morseCodeCharacterList.put("q", "--.-");
        morseCodeCharacterList.put("r", ".-.");
        morseCodeCharacterList.put("s", "...");
        morseCodeCharacterList.put("t", "-");
        morseCodeCharacterList.put("u", "..-");
        morseCodeCharacterList.put("v", "...-");
        morseCodeCharacterList.put("w", ".--");
        morseCodeCharacterList.put("x", "-..-");
        morseCodeCharacterList.put("y", "-.--");
        morseCodeCharacterList.put("z", "--..");

        morseCodeCharacterList.put("0", "-----");
        morseCodeCharacterList.put("1", ".----");
        morseCodeCharacterList.put("2", "..---");
        morseCodeCharacterList.put("3", "...--");
        morseCodeCharacterList.put("4", "....-");
        morseCodeCharacterList.put("5", ".....");
        morseCodeCharacterList.put("6", "-....");
        morseCodeCharacterList.put("7", "--...");
        morseCodeCharacterList.put("8", "---..");
        morseCodeCharacterList.put("9", "----.");

        morseCodeCharacterList.put(".", ".-.-.-");
        morseCodeCharacterList.put(",", "--..--");
        morseCodeCharacterList.put(" ", "");
        morseCodeCharacterList.put(":", "---...");
        morseCodeCharacterList.put(";", "-.-.-.");
        morseCodeCharacterList.put("?", "..--..");
        morseCodeCharacterList.put("-", "-....-");
        morseCodeCharacterList.put("_", "..--.-");
        morseCodeCharacterList.put("(", "-.--.-");
        morseCodeCharacterList.put(")", "-.--.-");
        morseCodeCharacterList.put("'", ".----.");
        morseCodeCharacterList.put("=", "-...-");
        morseCodeCharacterList.put("+", ".-.-.");
        morseCodeCharacterList.put("/", "-..-.");
        morseCodeCharacterList.put("@", ".--.-.");
        morseCodeCharacterList.put("", "");
        morseCodeCharacterList.put("","" );
    }
}
