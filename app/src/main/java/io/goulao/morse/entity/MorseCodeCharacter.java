package io.goulao.morse.entity;

import java.util.List;

/**
 * Created by NB21761 on 06/02/2016.
 */
public class MorseCodeCharacter {

    private String code;
    private List<Integer> times;

    public MorseCodeCharacter(String code, List<Integer> times) {
        this.times = times;
        this.code = code;
    }

    public MorseCodeCharacter() {
    }

    public List<Integer>getTimes() {
        return times;
    }

    public String getCode() {
        return code;
    }
}
